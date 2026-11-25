package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.dto.ResumoViradaDTO;
import br.com.cana.gestorcana_api.entity.StatusTemporada;
import br.com.cana.gestorcana_api.entity.Temporada;
import br.com.cana.gestorcana_api.exception.RegraNegocioException;
import br.com.cana.gestorcana_api.repository.ContribuicaoRepository;
import br.com.cana.gestorcana_api.repository.JogadorRepository;
import br.com.cana.gestorcana_api.repository.PartidaRepository;
import br.com.cana.gestorcana_api.repository.TemporadaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TemporadaService {

    private static final Logger log = LoggerFactory.getLogger(TemporadaService.class);

    // A mensalidade vence no dia 10 (RN18)
    private static final int DIA_VENCIMENTO = 10;

    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");

    private final TemporadaRepository repository;
    private final PartidaRepository partidaRepository;
    private final JogadorRepository jogadorRepository;
    private final ContribuicaoRepository contribuicaoRepository;
    private final Clock relogio;

    @Autowired
    public TemporadaService(TemporadaRepository repository,
                            PartidaRepository partidaRepository,
                            JogadorRepository jogadorRepository,
                            ContribuicaoRepository contribuicaoRepository) {
        this(repository, partidaRepository, jogadorRepository, contribuicaoRepository, Clock.system(FUSO_BRASIL));
    }

    // Usado nos testes para fixar a data de "hoje".
    TemporadaService(TemporadaRepository repository,
                     PartidaRepository partidaRepository,
                     JogadorRepository jogadorRepository,
                     ContribuicaoRepository contribuicaoRepository,
                     Clock relogio) {
        this.repository = repository;
        this.partidaRepository = partidaRepository;
        this.jogadorRepository = jogadorRepository;
        this.contribuicaoRepository = contribuicaoRepository;
        this.relogio = relogio;
    }

    public String salvar(Temporada t) {
        if (t == null || t.getAno() == null) {
            return "Erro: Dados da temporada inválidos.";
        }

        // Regra de Negócio: O ano deve ser válido
        if (t.getAno() < 2000 || t.getAno() > 2100) {
            System.err.println("Ano inválido!");
            return "Erro: Ano inválido (deve estar entre 2000 e 2100).";
        }

        // Regra de Negócio: Verificar se o ano já existe em cadastros novos
        if (t.getId() == null && repository.existsByAno(t.getAno())) {
            System.err.println("Temporada já cadastrada!");
            return "Erro: Temporada com o ano " + t.getAno() + " já está cadastrada!";
        }

        repository.save(t);
        return "OK";
    }

    public List<Temporada> listarTodas() {
        return repository.findAll();
    }

    public Optional<Temporada> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        return repository.findById(id);
    }

    public Optional<Temporada> buscarPorAno(Integer ano) {
        if (ano == null) return Optional.empty();
        return repository.findByAno(ano);
    }

    public Optional<Temporada> buscarVigente() {
        return repository.findByStatus(StatusTemporada.VIGENTE);
    }

    /**
     * Retorna false se a temporada não existe.
     * Lança RegraNegocioException se for a vigente ou se tiver partidas vinculadas.
     */
    public boolean deletar(Integer id) {
        if (id == null) {
            return false;
        }
        Optional<Temporada> temporada = repository.findById(id);
        if (temporada.isEmpty()) {
            return false;
        }
        if (temporada.get().getStatus() == StatusTemporada.VIGENTE) {
            throw new RegraNegocioException("Não é possível excluir a temporada vigente.");
        }
        if (partidaRepository.existsByTemporadaId(id)) {
            throw new RegraNegocioException("Não é possível excluir a temporada " + temporada.get().getAno()
                    + ": existem partidas vinculadas a ela.");
        }
        repository.deleteById(id);
        return true;
    }

    // --- VIRADA DE TEMPORADA ---

    /**
     * Resumo do que vai acontecer na virada (RF05). Não altera nada no banco.
     */
    @Transactional(readOnly = true)
    public ResumoViradaDTO gerarResumoVirada() {
        Temporada vigente = buscarVigente()
                .orElseThrow(() -> new RegraNegocioException("Não existe temporada vigente."));

        int anoAtual = vigente.getAno();
        int anoNovo = anoAtual + 1;

        long totalPartidas = partidaRepository.countByTemporadaId(vigente.getId());

        List<String> suspensos = new ArrayList<>(jogadorRepository.listarNomesSuspensosAtivos());
        suspensos.sort(String.CASE_INSENSITIVE_ORDER);

        long pendentes = contribuicoesPendentes(anoAtual);
        boolean loteGerado = contribuicaoRepository.existsByAno(anoNovo);

        List<String> avisos = new ArrayList<>();
        avisos.add("Fazer backup antes de confirmar.");
        if (!loteGerado) {
            avisos.add("Lote de contribuições de " + anoNovo + " ainda não foi gerado.");
        }
        if (!suspensos.isEmpty()) {
            avisos.add(suspensos.size() + " jogador(es) suspenso(s) serão liberados na virada.");
        }
        if (pendentes > 0) {
            avisos.add(pendentes + " contribuição(ões) de " + anoAtual + " continuam pendentes.");
        }
        if (anoNovo > anoCorrente() + 1) {
            avisos.add("A temporada " + anoNovo + " só pode ser aberta a partir de " + (anoNovo - 1) + ".");
        }
        if (repository.existsById(anoNovo) || repository.existsByAno(anoNovo)) {
            avisos.add("A temporada " + anoNovo + " já existe.");
        }

        return new ResumoViradaDTO(anoAtual, anoNovo, totalPartidas, suspensos, pendentes, loteGerado, avisos);
    }

    /**
     * Encerra a temporada vigente e abre a do ano seguinte (RN03: tudo ou nada).
     * Libera todos os suspensos (RN12). Não mexe em partidas, cartões nem contribuições:
     * os cartões antigos ficam no histórico e só os da temporada vigente contam.
     */
    @Transactional
    public Temporada virarTemporada() {
        Temporada vigente = buscarVigente()
                .orElseThrow(() -> new RegraNegocioException("Não existe temporada vigente."));

        int anoNovo = vigente.getAno() + 1;

        if (repository.existsById(anoNovo) || repository.existsByAno(anoNovo)) {
            throw new RegraNegocioException("A temporada " + anoNovo + " já existe.");
        }

        // Evita virar duas vezes por engano (ex.: clique duplo)
        if (anoNovo > anoCorrente() + 1) {
            throw new RegraNegocioException("A temporada " + anoNovo + " só pode ser aberta a partir de "
                    + (anoNovo - 1) + ".");
        }

        // O flush precisa acontecer antes do INSERT: o índice único parcial
        // ux_temporada_uma_vigente não aceita duas temporadas VIGENTE ao mesmo tempo.
        vigente.setStatus(StatusTemporada.ENCERRADA);
        repository.saveAndFlush(vigente);

        repository.inserirVigenteComIdIgualAoAno(anoNovo);

        int liberados = jogadorRepository.liberarTodosSuspensos();
        log.info("Virada para {}: {} jogador(es) suspenso(s) liberado(s).", anoNovo, liberados);

        return repository.findById(anoNovo)
                .orElseThrow(() -> new IllegalStateException("Temporada " + anoNovo + " não encontrada após a virada."));
    }

    private long contribuicoesPendentes(int ano) {
        return contribuicaoRepository.contarPendentesVencidas(ano, ultimoMesVencido(ano, LocalDate.now(relogio)));
    }

    /**
     * Último mês já vencido do ano (0 = nenhum). O mês atual só vence a partir do dia 11.
     */
    static int ultimoMesVencido(int ano, LocalDate hoje) {
        if (ano < hoje.getYear()) {
            return 12;
        }
        if (ano > hoje.getYear()) {
            return 0;
        }
        return hoje.getDayOfMonth() > DIA_VENCIMENTO ? hoje.getMonthValue() : hoje.getMonthValue() - 1;
    }

    private int anoCorrente() {
        return Year.now(relogio).getValue();
    }
}

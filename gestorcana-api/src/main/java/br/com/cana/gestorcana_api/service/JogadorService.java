package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Endereco;
import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.repository.ContribuicaoRepository;
import br.com.cana.gestorcana_api.repository.EnderecoRepository;
import br.com.cana.gestorcana_api.repository.EventoRepository;
import br.com.cana.gestorcana_api.repository.JogadorRepository;
import br.com.cana.gestorcana_api.repository.JogadorPartidaRepository;
import br.com.cana.gestorcana_api.util.TextoUtil;
import br.com.cana.gestorcana_api.util.ValidacaoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JogadorService {

    @Autowired
    private JogadorRepository jogadorRepository;

    @Autowired
    private br.com.cana.gestorcana_api.repository.JogadorPartidaRepository jogadorPartidaRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private ContribuicaoRepository contribuicaoRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public JogadorService() {
    }

    public JogadorService(JogadorRepository jogadorRepository, EnderecoRepository enderecoRepository, JogadorPartidaRepository jogadorPartidaRepository) {
        this.jogadorRepository = jogadorRepository;
        this.enderecoRepository = enderecoRepository;
        this.jogadorPartidaRepository = jogadorPartidaRepository;
    }

    /**
     * Salva um novo jogador após validar todos os campos.
     */
    public String salvarJogador(Jogador j) {
        String erro = validarDadosCompletos(j);
        if (erro != null)
            return erro;

        if (isNumeroCamisaEmUso(j.getNumCamisa(), j.getId())) {
            return "A camisa número " + j.getNumCamisa() + " já pertence a outro jogador!";
        }

        j.setNome(TextoUtil.normalizar(j.getNome()));
        j.setAtivo(true); // só jogadores ativos aparecem para cadastro/edição
        normalizarEndereco(j.getEndereco());

        try {
            if (j.getEndereco() != null && j.getEndereco().getId() == null) {
                Endereco endSalvo = enderecoRepository.save(j.getEndereco());
                j.setEndereco(endSalvo);
            }
            jogadorRepository.save(j);
            return "OK";
        } catch (Exception ex) {
            return "Erro ao persistir dados no banco: " + ex.getMessage();
        }
    }

    /**
     * Atualiza um jogador existente, validando ID e dados.
     */
    public String atualizarJogador(Jogador j) {
        if (j == null || j.getId() == null || j.getId() <= 0) {
            return "ID inválido para atualização.";
        }

        String erro = validarDadosCompletos(j);
        if (erro != null) {
            return erro;
        }

        j.setNome(TextoUtil.normalizar(j.getNome()));
        // Os apps não mandam "ativo": mantém o que está no banco (reativar é pelo
        // endpoint próprio, não por edição)
        j.setAtivo(jogadorRepository.findById(j.getId()).map(Jogador::getAtivo).orElse(true));
        normalizarEndereco(j.getEndereco());

        try {
            if (j.getEndereco() != null) {
                enderecoRepository.save(j.getEndereco());
            }
            jogadorRepository.save(j);
            return "OK";
        } catch (Exception ex) {
            return "Erro ao atualizar dados: " + ex.getMessage();
        }
    }

    /**
     * Exclui um jogador baseado no ID.
     */
    public boolean excluirJogador(int id) {
        if (!ValidacaoUtil.validarId(id)) {
            return false;
        }
        try {
            jogadorRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Busca todos os jogadores cadastrados.
     */
    public List<Jogador> listarTodos() {
        List<Jogador> todos = listarAtivos();
        for (Jogador j : todos) {
            // Trava para evitar null pointer ou falso inadimplente
            if (j.getMensalidadeEmDia() == null) {
                j.setMensalidadeEmDia(false);
            }
            if (j.getEstaSuspenso() == null) {
                j.setEstaSuspenso(false);
            }
        }
        return todos;
    }

    /**
     * Busca jogadores que podem ser padrinhos (evita que um jogador seja padrinho
     * de si mesmo).
     */
    public List<Jogador> buscarPadrinhos(int idAtual) {
        return listarAtivos().stream()
                .filter(j -> j.getId() != null && j.getId() != idAtual)
                .collect(Collectors.toList());
    }

    /**
     * Filtra jogadores por status (ATIVO, SUSPENSO, etc).
     */
    public List<Jogador> filtrarPorStatus(String status) {
        return filtrarPorStatus(status, false);
    }

    /**
     * Com incluirInativos=true, traz também os excluídos com histórico (usado só
     * na pesquisa de jogadores, para permitir reativar).
     */
    public List<Jogador> filtrarPorStatus(String status, boolean incluirInativos) {
        if (incluirInativos) {
            return jogadorRepository.findAll();
        }
        if (status == null || status.trim().isEmpty()) {
            return listarAtivos();
        }
        return listarAtivos().stream()
                .filter(j -> j.getStatus() != null && j.getStatus().equalsIgnoreCase(status.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Busca por ID incluindo inativos (partidas antigas ainda referenciam eles).
     */
    public Optional<Jogador> buscarPorId(int id) {
        return jogadorRepository.findById(id);
    }

    // --- MÉTODOS PRIVADOS E AUXILIARES ---

    /** Jogadores inativos (excluídos com histórico) ficam fora de todas as listagens. */
    private List<Jogador> listarAtivos() {
        return jogadorRepository.findAll().stream()
                .filter(Jogador::getAtivo)
                .collect(Collectors.toList());
    }

    private boolean isNumeroCamisaEmUso(Integer numCamisa, Integer idJogador) {
        if (numCamisa == null)
            return false;
        // Camisa de jogador inativo fica livre para outro usar
        List<Jogador> todos = listarAtivos();
        for (Jogador j : todos) {
            if (Objects.equals(j.getNumCamisa(), numCamisa)) {
                if (idJogador == null || !Objects.equals(j.getId(), idJogador)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String validarDadosCompletos(Jogador j) {
        if (j == null) {
            return "Dados do jogador não informados.";
        }
        if (ValidacaoUtil.isVazio(j.getNome())) {
            return "O nome do jogador é obrigatório.";
        }
        if (j.getApelido() == null || j.getApelido().trim().isEmpty()) {
            return "O campo Apelido é obrigatório.";
        }
        if (!ValidacaoUtil.validarRG(j.getRg())) {
            return "RG inválido ou não informado.";
        }
        if (!ValidacaoUtil.validarCPF(j.getCpf())) {
            return "CPF inválido ou não informado.";
        }
        if (!ValidacaoUtil.validarTelefone(j.getTelefone())) {
            return "Telefone inválido (insira o DDD e números).";
        }
        if (j.getNivel() != null && !ValidacaoUtil.validarNivel(j.getNivel())) {
            return "O nível técnico deve estar entre 1 e 100.";
        }
        if (j.getDataNascimento() == null) {
            return "A data de nascimento é obrigatória.";
        }

        return validarEndereco(j.getEndereco());
    }

    private void normalizarEndereco(Endereco e) {
        if (e != null) {
            e.setLogradouro(TextoUtil.normalizar(e.getLogradouro()));
            e.setBairro(TextoUtil.normalizar(e.getBairro()));
            e.setCidade(TextoUtil.normalizar(e.getCidade()));
            if (e.getEstado() != null) {
                e.setEstado(e.getEstado().toUpperCase());
            }
            if (e.getComplemento() != null) {
                e.setComplemento(TextoUtil.normalizar(e.getComplemento()));
            }
        }
    }

    private String validarEndereco(Endereco e) {
        if (e == null)
            return "Os dados de endereço são obrigatórios.";
        if (ValidacaoUtil.isVazio(e.getLogradouro()))
            return "A rua é obrigatória.";
        if (ValidacaoUtil.isVazio(e.getBairro()))
            return "O bairro é obrigatório.";
        if (ValidacaoUtil.isVazio(e.getCidade()))
            return "A cidade é obrigatória.";
        if (ValidacaoUtil.isVazio(e.getEstado()))
            return "O estado (UF) é obrigatório.";
        if (ValidacaoUtil.isVazio(e.getCep()))
            return "O CEP é obrigatório.";

        return null;
    }

    public String excluir(int id) {
        Optional<Jogador> opt = jogadorRepository.findById(id);
        if (opt.isEmpty()) {
            return "Jogador não encontrado.";
        }

        Jogador jogador = opt.get();
        boolean temHistorico = jogadorPartidaRepository.existsByJogadorId(id)
                || eventoRepository.existsByJogadorId(id)
                || contribuicaoRepository.existsByJogadorIdAndPago(id, 1);

        try {
            if (temHistorico) {
                // Partidas, eventos e mensalidades pagas precisam continuar no histórico
                // (e o banco tem FK para jogador): só some das listagens.
                jogador.setAtivo(false);
                jogadorRepository.save(jogador);
                return "OK";
            }

            // Sem histórico: exclusão de verdade, levando junto as cobranças pendentes
            // e o vínculo de padrinho dos afilhados (ambos com FK para jogador).
            transactionTemplate.executeWithoutResult(status -> {
                for (Jogador afilhado : jogadorRepository.findByPadrinhoId(id)) {
                    afilhado.setPadrinhoId(null);
                    jogadorRepository.save(afilhado);
                }
                contribuicaoRepository.deletarPendentesPorJogador(id);
                jogadorRepository.deleteById(id);
                jogadorRepository.flush();
            });
            return "OK";
        } catch (Exception ex) {
            return "Não foi possível excluir o jogador: " + ex.getMessage();
        }
    }

    public String reativar(int id) {
        Optional<Jogador> opt = jogadorRepository.findById(id);
        if (opt.isEmpty()) {
            return "Jogador não encontrado.";
        }
        Jogador jogador = opt.get();
        if (jogador.getAtivo()) {
            return "OK";
        }

        // A camisa ficou livre enquanto ele estava inativo: se outro jogador pegou,
        // ele volta sem número em vez de duplicar.
        String aviso = "";
        if (isNumeroCamisaEmUso(jogador.getNumCamisa(), jogador.getId())) {
            aviso = " A camisa " + jogador.getNumCamisa() + " já pertence a outro jogador, então ele voltou sem número.";
            jogador.setNumCamisa(null);
        }

        jogador.setAtivo(true);
        jogadorRepository.save(jogador);
        return "OK" + aviso;
    }

    public String obterNomeAtivo(String texto) {
        if (texto == null || texto.trim().isEmpty() || texto.equals("Selecione...") || texto.equals("Não escalado")) {
            return "";
        }

        String nomeAtivo = texto;
        if (texto.contains(" / ")) {
            String[] partes = texto.split(" / ");
            nomeAtivo = partes[partes.length - 1].trim();
        }

        if (nomeAtivo.contains(" (")) {
            return nomeAtivo.substring(0, nomeAtivo.indexOf(" (")).trim();
        }

        return nomeAtivo.trim();
    }

    public void processarLimpesaDeSuspensaoPosJogo(List<String> nomesDaArbitragem) {
        try {
            for (String nomeRaw : nomesDaArbitragem) {
                String nomeLimpo = obterNomeAtivo(nomeRaw);
                if (nomeLimpo.isEmpty())
                    continue;

                Jogador jogador = buscarPorNomeOuApelido(nomeLimpo);

                if (jogador != null && Boolean.TRUE.equals(jogador.getEstaSuspenso())) {
                    jogador.setEstaSuspenso(false);
                    jogadorRepository.save(jogador);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sincronizarStatusSuspensao(Jogador j) {
        if (j == null || j.getId() == null) {
            return;
        }

        // 1. Pega os cartões iniciais cadastrados (se houver)
        int totalAmarelos = j.getcAmarelosIniciais() != null ? j.getcAmarelosIniciais() : 0;
        int totalVermelhos = j.getcVermelhosIniciais() != null ? j.getcVermelhosIniciais() : 0;

        // 2. Soma os cartões reais tomados em todas as partidas
        List<br.com.cana.gestorcana_api.entity.JogadorPartida> atuacoes = jogadorPartidaRepository
                .findByJogadorId(j.getId());
        if (atuacoes != null) {
            for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : atuacoes) {
                totalAmarelos += (jp.getCartaoAmarelo() != null ? jp.getCartaoAmarelo() : 0);
                totalVermelhos += (jp.getCartaoVermelho() != null ? jp.getCartaoVermelho() : 0);
            }
        }

        // 3. Regra CANA: Suspenso se acumulou 3 amarelos ou tomou vermelho direto
        boolean suspensoPorAmarelo = (totalAmarelos > 0 && totalAmarelos % 3 == 0);
        boolean suspensoPorVermelho = totalVermelhos > 0;

        // Se já estiver marcado manualmente ou pelos critérios acima:
        boolean statusReal = Boolean.TRUE.equals(j.getEstaSuspenso()) || suspensoPorAmarelo || suspensoPorVermelho;

        j.setEstaSuspenso(statusReal);
        j.setStatus(statusReal ? "Suspenso" : "Ativo");
    }

    public Jogador buscarPorNomeOuApelido(String nomeOuApelido) {
        if (nomeOuApelido == null || nomeOuApelido.trim().isEmpty()) {
            return null;
        }
        String busca = nomeOuApelido.trim();
        // Ativos primeiro: um inativo pode ter o mesmo apelido de um jogador atual
        return jogadorRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing((Jogador j) -> !j.getAtivo()))
                .filter(j -> (j.getApelido() != null && j.getApelido().equalsIgnoreCase(busca))
                        || (j.getNome() != null && j.getNome().equalsIgnoreCase(busca)))
                .findFirst()
                .orElse(null);
    }
}
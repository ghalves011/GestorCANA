package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Endereco;
import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.repository.ContribuicaoRepository;
import br.com.cana.gestorcana_api.repository.EnderecoRepository;
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
        List<Jogador> todos = jogadorRepository.findAll();
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
        return jogadorRepository.findAll().stream()
                .filter(j -> j.getId() != null && j.getId() != idAtual)
                .collect(Collectors.toList());
    }

    /**
     * Filtra jogadores por status (ATIVO, SUSPENSO, etc).
     */
    public List<Jogador> filtrarPorStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return jogadorRepository.findAll();
        }
        return jogadorRepository.findAll().stream()
                .filter(j -> j.getStatus() != null && j.getStatus().equalsIgnoreCase(status.trim()))
                .collect(Collectors.toList());
    }

    // --- MÉTODOS PRIVADOS E AUXILIARES ---

    private boolean isNumeroCamisaEmUso(Integer numCamisa, Integer idJogador) {
        if (numCamisa == null)
            return false;
        List<Jogador> todos = jogadorRepository.findAll();
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

        try {
            // Contribuicao não tem FK para jogador: sem apagar as cobranças junto, elas
            // ficariam órfãs no banco. Tudo numa transação só — se o jogador não puder
            // ser excluído (ex.: tem partidas), as cobranças são restauradas.
            transactionTemplate.executeWithoutResult(status -> {
                contribuicaoRepository.deletarPorJogador(id);
                jogadorRepository.deleteById(id);
                jogadorRepository.flush();
            });
            return "OK";
        } catch (Exception ex) {
            return "Não foi possível excluir o jogador. Verifique se ele possui partidas ou mensalidades registradas.";
        }
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
        return jogadorRepository.findAll().stream()
                .filter(j -> (j.getApelido() != null && j.getApelido().equalsIgnoreCase(busca))
                        || (j.getNome() != null && j.getNome().equalsIgnoreCase(busca)))
                .findFirst()
                .orElse(null);
    }
}
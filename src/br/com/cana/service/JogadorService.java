package br.com.cana.service;

import java.util.List;

import br.com.cana.dao.EnderecoDAO;
import br.com.cana.dao.JogadorDAO;
import br.com.cana.model.Endereco;
import br.com.cana.model.Jogador;
import br.com.cana.util.TextoUtil;
import br.com.cana.util.ValidacaoUtil;

public class JogadorService {

    private JogadorDAO jogadorDAO;
    private EnderecoDAO enderecoDAO;

    public JogadorService() {
        this.jogadorDAO = new JogadorDAO();
        this.enderecoDAO = new EnderecoDAO();
    }

    /**
     * Salva um novo jogador após validar todos os campos.
     */
    public String salvarJogador(Jogador j) {
        // 1. Validações de negócio (Campos obrigatórios)
        String erro = validarDadosCompletos(j);
        if (erro != null)
            return erro;

        // 2. REGRA DE OURO: Validar se o número da camisa já está em uso
        // Passamos o ID atual para que, se for uma edição, ele não barra o próprio
        // número
        if (jogadorDAO.isNumeroCamisaEmUso(j.getNumCamisa(), j.getId())) {
            return "A camisa número " + j.getNumCamisa() + " já pertence a outro jogador!";
        }

        // 3. Normalização
        j.setNome(TextoUtil.normalizar(j.getNome()));
        normalizarEndereco(j.getEndereco());

        // 4. Decisão: INSERIR ou ATUALIZAR?
        if (j.getId() == 0) {
            // --- FLUXO DE NOVO JOGADOR ---
            int idEnderecoGerado = enderecoDAO.inserir(j.getEndereco());
            if (idEnderecoGerado > 0) {
                j.getEndereco().setId(idEnderecoGerado);
                return jogadorDAO.inserir(j) ? "OK" : "Erro ao persistir novo jogador.";
            } else {
                return "Erro ao salvar os dados de endereço.";
            }
        } else {
            // --- FLUXO DE ALTERAÇÃO (UPDATE) ---
            // Primeiro atualizamos o endereço que já existe
            boolean enderecoOk = enderecoDAO.atualizar(j.getEndereco());
            if (enderecoOk) {
                // Depois atualizamos os dados do jogador
                return jogadorDAO.atualizar(j) ? "OK" : "Erro ao atualizar dados do jogador.";
            } else {
                return "Erro ao atualizar o endereço do jogador.";
            }
        }
    }

    /**
     * Atualiza um jogador existente, validando ID e dados.
     */
    public String atualizarJogador(Jogador j) {
        if (!ValidacaoUtil.validarId(j.getId())) {
            return "ID inválido para atualização.";
        }

        String erro = validarDadosCompletos(j);
        if (erro != null) {
            return erro;
        }

        j.setNome(TextoUtil.normalizar(j.getNome()));
        normalizarEndereco(j.getEndereco());

        // Atualiza as duas partes
        boolean enderecoOk = enderecoDAO.atualizar(j.getEndereco());
        boolean jogadorOk = jogadorDAO.atualizar(j);

        return (enderecoOk && jogadorOk) ? "OK" : "Erro ao atualizar dados.";
    }

    /**
     * Exclui um jogador baseado no ID.
     */
    public boolean excluirJogador(int id) {
        if (!ValidacaoUtil.validarId(id)) {
            return false;
        }
        return jogadorDAO.excluir(id);
    }

    /**
     * Busca todos os jogadores cadastrados.
     */
    public List<Jogador> listarTodos() {
        return jogadorDAO.listarTodos();
    }

    /**
     * Busca jogadores que podem ser padrinhos (evita que um jogador seja padrinho
     * de si mesmo).
     */
    public List<Jogador> buscarPadrinhos(int idAtual) {
        return jogadorDAO.listarPadrinhosPossiveis(idAtual);
    }

    /**
     * Filtra jogadores por status (ATIVO, SUSPENSO, etc).
     */
    public List<Jogador> filtrarPorStatus(String status) {
        return jogadorDAO.buscarPorStatus(status);
    }

    // --- MÉTODO PRIVADO DE SUPORTE ---

    /**
     * Centraliza todas as regras de validação para evitar repetição no salvar e
     * atualizar.
     */
    private String validarDadosCompletos(Jogador j) {
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
        if (!ValidacaoUtil.validarNivel(j.getNivel())) {
            return "O nível técnico deve estar entre 1 e 100.";
        }
        if (j.getDataNascimento() == null) {
            return "A data de nascimento é obrigatória.";
        }

        // Se chegou aqui, não há erros
        return validarEndereco(j.getEndereco());
    }

    private void normalizarEndereco(Endereco e) {
        if (e != null) {
            e.setLogradouro(TextoUtil.normalizar(e.getLogradouro()));
            e.setBairro(TextoUtil.normalizar(e.getBairro()));
            e.setCidade(TextoUtil.normalizar(e.getCidade()));
            e.setEstado(e.getEstado().toUpperCase()); // Estado sempre em MAIÚSCULO
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

        return null; // Se tudo estiver preenchido, retorna null (sem erros)
    }

    public String excluir(int id) {
        // 1. Buscamos o jogador completo para descobrir qual o ID do endereço dele
        Jogador jogadorParaExcluir = jogadorDAO.buscarPorId(id);

        if (jogadorParaExcluir == null) {
            return "Jogador não encontrado.";
        }

        // Guardamos o ID do endereço antes de apagar o jogador
        int idEndereco = jogadorParaExcluir.getEndereco().getId();

        // 2. Tentamos excluir o jogador primeiro
        // (Importante: Se ele tiver gols ou partidas, o banco vai barrar aqui)
        boolean deletouJogador = jogadorDAO.excluir(id);

        if (deletouJogador) {
            // 3. Agora que o jogador sumiu, limpamos o endereço dele
            enderecoDAO.excluir(idEndereco);
            return "OK";
        } else {
            return "Não foi possível excluir o jogador. Verifique se ele possui partidas ou mensalidades registradas.";
        }
    }

    public String obterNomeAtivo(String texto) {
        if (texto == null || texto.trim().isEmpty()
                || texto.equals("Selecione...") || texto.equals("Não escalado")) {
            return "";
        }

        String nomeAtivo = texto;
        // Se tiver o padrão de substituição " / ", pega apenas o último (que seria o
        // ativo)
        if (texto.contains(" / ")) {
            String[] partes = texto.split(" / ");
            nomeAtivo = partes[partes.length - 1].trim();
        }

        // Remove as posições de strings como "Gui (ATA)" se necessário
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

                Jogador jogador = jogadorDAO.buscarPorNomeOuApelido(nomeLimpo);

                if (jogador != null && jogador.isEstaSuspenso()) {
                    // 🌟 ZERAGEM: O cumprimento da pena limpa a ficha!
                    jogador.setEstaSuspenso(false);
                    jogador.setcAmarelosIniciais(0); // Zera amarelos
                    jogador.setcVermelhosIniciais(0); // Zera vermelhos

                    // Persiste a limpeza no banco
                    jogadorDAO.atualizar(jogador);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sincronizarStatusSuspensao(Jogador j) {
        // Aplica a regra matemática: 3 amarelos ou 1 vermelho = suspenso
        boolean statusReal = (j.getcAmarelosIniciais() >= 3 || j.getcVermelhosIniciais() >= 1);
        j.setEstaSuspenso(statusReal);
    }

    /**
     * Busca jogador por nome ou apelido, repassando a requisição para o DAO.
     */
    public Jogador buscarPorNomeOuApelido(String nomeOuApelido) {
        return jogadorDAO.buscarPorNomeOuApelido(nomeOuApelido);
    }

}
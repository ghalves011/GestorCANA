package br.com.cana.service;

import java.util.ArrayList;
import java.util.List;
import br.com.cana.dao.ContribuicaoDAO;
import br.com.cana.dao.JogadorDAO;
import br.com.cana.model.Contribuicao;
import br.com.cana.model.Jogador; // <--- Supondo que sua classe de modelo chame Jogador
import br.com.cana.util.ValidacaoUtil;

public class ContribuicaoService {

    private ContribuicaoDAO contribuicaoDAO;
    private JogadorDAO jogadorDAO;

    public ContribuicaoService() {
        this.contribuicaoDAO = new ContribuicaoDAO();
        this.jogadorDAO = new JogadorDAO();
    }

    public String gerarCobranca(Contribuicao c) {
        if (!ValidacaoUtil.validarMes(c.getMes()))
            return "Mês inválido.";
        if (!ValidacaoUtil.validarValorPositivo(c.getValor()))
            return "Valor inválido.";

        if (contribuicaoDAO.isMensalidadePaga(c.getJogador().getId(), c.getMes(), c.getAno())) {
            return "Já existe cobrança para este período.";
        }

        boolean sucesso = contribuicaoDAO.inserir(c);
        return sucesso ? "OK" : "Erro ao registrar cobrança.";
    }

    /**
     * NOVO: Gera em lote as 12 mensalidades do ano para todos os jogadores
     * cadastrados.
     * Reaproveita totalmente as regras e validações do seu método gerarCobranca.
     */
    public String gerarLoteAnual(int ano, double valor) {
        // Busca a lista de jogadores que você já tem no seu JogadorDAO
        List<Jogador> jogadores = jogadorDAO.listarTodos();

        if (jogadores == null || jogadores.isEmpty()) {
            return "Nenhum jogador cadastrado para gerar o lote.";
        }

        int totalGerado = 0;

        // Roda cada jogador e cria as 12 referências do ano no banco
        for (Jogador jogador : jogadores) {
            for (int mes = 1; mes <= 12; mes++) {

                // --- [TRAVA DE SEGURANÇA AQUI] ---
                // Pergunta ao DAO se essa combinação de Jogador + Mês + Ano já existe no banco
                if (contribuicaoDAO.existeContribuicao(jogador.getId(), mes, ano)) {
                    continue; // Se já existir, o 'continue' pula direto para o próximo mês sem duplicar!
                }

                Contribuicao c = new Contribuicao();
                c.setJogador(jogador);
                c.setAno(ano);
                c.setMes(mes);
                c.setValor(valor);

                // Chama o seu método existente com todas as validações mecânicas
                String resultado = gerarCobranca(c);
                if ("OK".equals(resultado)) {
                    totalGerado++;
                }
            }
        }

        return totalGerado > 0 ? "OK" : "Nenhuma nova cobrança precisou ser gerada (todas já existem).";
    }

    public String registrarPagamento(int jogadorId, int mes, int ano, double valor) {
        if (!ValidacaoUtil.validarValorPositivo(valor))
            return "Valor inválido.";

        // 1. Tenta dar baixa na contribuição
        boolean sucessoPagamento = contribuicaoDAO.baixarPagamento(jogadorId, mes, ano, valor);

        if (sucessoPagamento) {
            // 2. INTEGRAÇÃO REAL: Se o pagamento foi ok, atualiza o status do jogador!
            jogadorDAO.atualizarStatus(jogadorId, "Em dia");
            return "OK";
        }

        return "Não foi possível encontrar a mensalidade.";
    }

    public boolean podeJogar(int jogadorId) {
        // 1. Busca o jogador no banco de dados para descobrir a data de admissão
        JogadorDAO jogadorDAO = new JogadorDAO();
        Jogador jogador = jogadorDAO.buscarPorId(jogadorId);

        // 2. Extrai a data (com uma proteção extra caso o ID não seja encontrado)
        java.time.LocalDate admissao = (jogador != null) ? jogador.getDataAdmissao() : null;

        // 3. Verifica as pendências usando a nova regra blindada!
        return !contribuicaoDAO.hasPendencias(jogadorId, admissao);
    }

    /**
     * Busca todos os jogadores e monta a matriz de contribuições (Jan a Dez)
     * usando o método isMensalidadePaga que você já criou no ContribuicaoDAO.
     */
    public List<Object[]> obterMatrizContribuicoes(int ano, String busca) {
        List<Object[]> matriz = new ArrayList<>();

        // 1. Busca todos os jogadores cadastrados no sistema
        // (Caso seu método no JogadorDAO tenha outro nome, como listarAtivos, altere
        // aqui)
        List<br.com.cana.model.Jogador> jogadores = jogadorDAO.listarTodos();

        if (jogadores == null)
            return matriz;

        // 2. Passa de jogador em jogador cruzando com os 12 meses
        for (br.com.cana.model.Jogador j : jogadores) {

            // Aplica o filtro de busca da tela direto no nome do jogador (ignora
            // maiúsculas/minúsculas)
            if (busca != null && !busca.trim().isEmpty()) {
                if (!j.getNome().toLowerCase().contains(busca.toLowerCase().trim())) {
                    continue; // Pula para o próximo jogador se não bater com a busca
                }
            }

            // Criamos o array da linha: [0] ID, [1] Nome, e [2 até 13] para os 12 meses
            Object[] linha = new Object[14];
            linha[0] = j.getId();
            linha[1] = j.getNome();

            // 3. O laço roda de 1 a 12 preenchendo o status de cada mês direto do banco
            for (int mes = 1; mes <= 12; mes++) {
                // Usa o seu método real do ContribuicaoDAO!
                boolean estaPago = contribuicaoDAO.isMensalidadePaga(j.getId(), mes, ano);
                linha[mes + 1] = estaPago; // Preenche as posições de 2 a 13 do array
            }

            matriz.add(linha);
        }

        return matriz;
    }

    public String obterTextoRecibo(int idJogador, int mes, int ano) {
        // Busca o registro definitivo no banco através do seu DAO
        Contribuicao c = contribuicaoDAO.buscarPorJogadorMesAno(idJogador, mes, ano);
        if (c != null) {
            return c.getReciboFormatado(); // Chama o método que você mostrou!
        }
        return "Erro: Contribuição não encontrada no banco.";
    }

    public String excluirPagamento(int idJogador, int mes, int ano) {
        // Aciona o DAO para deletar o registro no SQLite
        boolean deletado = contribuicaoDAO.excluir(idJogador, mes, ano);

        if (deletado) {
            return "OK";
        } else {
            return "Não foi possível excluir o registro. Verifique a conexão com o banco.";
        }
    }
}

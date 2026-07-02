package br.com.cana.dao;

import br.com.cana.model.Contribuicao;
import br.com.cana.model.Jogador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.ResultSet;
import br.com.cana.util.ConnectionFactory;
import br.com.cana.util.DateUtil;

public class ContribuicaoDAO {

    public boolean inserir(Contribuicao cont) {
        String sql = "INSERT INTO Contribuicao (jogador_id, mes, ano, valor, pago, dataPagamento) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, cont.getJogador().getId());
            stmt.setInt(2, cont.getMes());
            stmt.setInt(3, cont.getAno());
            stmt.setDouble(4, cont.getValor());
            stmt.setBoolean(5, cont.isPago());

            stmt.setString(6, DateUtil.paraBanco(cont.getDataPagamento()));

            stmt.executeUpdate();
            System.out.println("✅ Mensalidade registrada no banco!");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao registrar contribuição: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o jogador já pagou o mês atual.
     */
    public boolean isMensalidadePaga(int jogadorId, int mes, int ano) {
        // 1. Query blindada para evitar SQL Injection
        String sql = "SELECT pago FROM Contribuicao WHERE jogador_id = ? AND mes = ? AND ano = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // 2. Preenche os parâmetros da query com segurança
            stmt.setInt(1, jogadorId);
            stmt.setInt(2, mes);
            stmt.setInt(3, ano);

            // 3. Executa a query e obtém o ResultSet
            try (ResultSet rs = stmt.executeQuery()) {
                // Se o banco encontrar uma linha correspondente...
                if (rs.next()) {
                    // 4. Retorna o valor do campo "pago" (true/false)
                    return rs.getBoolean("pago");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao verificar financeiro: " + e.getMessage());
        }

        // Se não encontrar a linha ou der erro, assumimos que não está pago
        return false;
    }

    public boolean hasPendencias(int jogadorId, LocalDate dataAdmissao) {
        // 1. Pegamos a data atual e aplicamos a regra do dia 10
        LocalDate hoje = LocalDate.now();
        int diaAtual = hoje.getDayOfMonth();
        int mesAtual = hoje.getMonthValue();
        int anoAtual = hoje.getYear();

        // REGRA DO DIA 10: Se ainda não passou do dia 10, o mês atual NÃO está vencido.
        // Portanto, a cobrança obrigatória vai até o mês anterior!
        if (diaAtual <= 10) {
            mesAtual--;
            if (mesAtual == 0) { // Se for janeiro antes do dia 10, recua para dezembro do ano passado
                mesAtual = 12;
                anoAtual--;
            }
        }

        // 2. Se a data de admissão for nula, assumimos que o jogador é antigo
        int mesAdmissao = (dataAdmissao != null) ? dataAdmissao.getMonthValue() : mesAtual;
        int anoAdmissao = (dataAdmissao != null) ? dataAdmissao.getYear() : anoAtual;

        // 3. Se o jogador foi admitido depois do limite máximo de cobrança calculado,
        // ele está zerado
        if (anoAdmissao > anoAtual || (anoAdmissao == anoAtual && mesAdmissao > mesAtual)) {
            return false;
        }

        // 4. Calculamos quantos meses o jogador realmente deveria ter pago até o limite
        // corrigido
        int mesesDeveriaTerPago = 0;
        if (anoAdmissao == anoAtual) {
            mesesDeveriaTerPago = (mesAtual - mesAdmissao) + 1;
        } else {
            mesesDeveriaTerPago = (12 - mesAdmissao + 1) + ((anoAtual - anoAdmissao - 1) * 12) + mesAtual;
        }

        // 5. Query blindada (permanece igual, mas usando os limites corrigidos)
        String sql = "SELECT COUNT(*) FROM Contribuicao " +
                "WHERE jogador_id = ? AND pago = 1 " +
                "AND ((ano * 100) + mes) >= ? " +
                "AND ((ano * 100) + mes) <= ?";

        int limiteAdmissao = (anoAdmissao * 100) + mesAdmissao;
        int limiteHoje = (anoAtual * 100) + mesAtual;

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, jogadorId);
            stmt.setInt(2, limiteAdmissao);
            stmt.setInt(3, limiteHoje);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int mesesPagos = rs.getInt(1);
                    return mesesPagos < mesesDeveriaTerPago;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao verificar pendências: " + e.getMessage());
        }
        return false;
    }

    public boolean baixarPagamento(int jogadorId, int mes, int ano, double valorPago) {
        String sql = "UPDATE Contribuicao SET pago = 1, dataPagamento = ?, valor = ? " +
                "WHERE jogador_id = ? AND mes = ? AND ano = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, DateUtil.paraBanco(LocalDate.now()));
            stmt.setDouble(2, valorPago);
            stmt.setInt(3, jogadorId);
            stmt.setInt(4, mes);
            stmt.setInt(5, ano);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erro ao baixar pagamento: " + e.getMessage());
            return false;
        }
    }

    public Contribuicao buscarPorJogadorMesAno(int jogadorId, int mes, int ano) {
        // SQL com JOIN para trazer os dados da contribuição + o nome do jogador
        String sql = "SELECT c.*, j.nome AS nome_jogador " +
                "FROM Contribuicao c " +
                "JOIN jogador j ON c.jogador_id = j.id " +
                "WHERE c.jogador_id = ? AND c.mes = ? AND c.ano = ?";

        // try-with-resources: fecha conexão e comandos automaticamente
        try (Connection conn = ConnectionFactory.getConnection(); // <--- Ajuste para a sua classe de conexão
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jogadorId);
            stmt.setInt(2, mes);
            stmt.setInt(3, ano);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Preenche o objeto Contribuição com os dados do banco
                    Jogador jogador = new Jogador();
                    jogador.setId(jogadorId);
                    jogador.setNome(rs.getString("nome_jogador"));

                    Contribuicao c = new Contribuicao();
                    c.setJogador(jogador); // <--- Vincula o jogador preenchido
                    c.setMes(rs.getInt("mes"));
                    c.setAno(rs.getInt("ano"));
                    c.setValor(rs.getDouble("valor"));

                    c.setPago(rs.getInt("pago") == 1);

                    String dataTexto = rs.getString("dataPagamento");

                    if (dataTexto != null && !dataTexto.trim().isEmpty()) {
                        try {
                            // Se o banco salvou com data e hora (ex: 2026-05-15 19:30:00), pegamos só a
                            // data (2026-05-15)
                            if (dataTexto.contains(" ")) {
                                dataTexto = dataTexto.split(" ")[0];
                            }

                            // Se estiver no formato padrão do banco (yyyy-MM-dd)
                            if (dataTexto.contains("-")) {
                                c.setDataPagamento(java.time.LocalDate.parse(dataTexto));
                            }
                            // Se estiver salvo no formato brasileiro (dd/MM/yyyy)
                            else if (dataTexto.contains("/")) {
                                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                                        .ofPattern("dd/MM/yyyy");
                                c.setDataPagamento(java.time.LocalDate.parse(dataTexto, fmt));
                            }
                        } catch (Exception e) {
                            System.out.println("Não foi possível decifrar a data do banco: " + dataTexto);
                            c.setDataPagamento(null);
                        }
                    } else {
                        c.setDataPagamento(null);
                    }

                    return c;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar contribuição para recibo: " + e.getMessage());
        }
        return null; // Retorna null se não achar nada no banco
    }

    public boolean excluir(int idJogador, int mes, int ano) {
        // Query para estornar o pagamento, ou seja, marcar como não pago e limpar a
        // data. Não deletamos
        String sql = "UPDATE contribuicao SET pago = 0, dataPagamento = NULL " +
                "WHERE jogador_id = ? AND mes = ? AND ano = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idJogador);
            stmt.setInt(2, mes);
            stmt.setInt(3, ano);

            int linhasAfetadas = stmt.executeUpdate();

            // Se pelo menos uma linha foi atualizada, consideramos que o estorno foi
            // bem-sucedido
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.out.println("Erro ao estornar pagamento no DAO: " + e.getMessage());
            return false;
        }
    }

    public boolean existeContribuicao(int idJogador, int mes, int ano) {
        // Query super leve que apenas checa se existe a linha
        String sql = "SELECT 1 FROM contribuicao WHERE jogador_id = ? AND mes = ? AND ano = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idJogador);
            stmt.setInt(2, mes);
            stmt.setInt(3, ano);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Se tiver pelo menos uma linha, existe a contribuição
            }

        } catch (SQLException e) {
            System.out.println("Erro ao verificar existência de contribuição: " + e.getMessage());
            return false;
        }
    }
}
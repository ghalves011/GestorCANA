package br.com.cana.dao;

import br.com.cana.model.Partida;
import br.com.cana.model.JogadorPartida;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import br.com.cana.util.ConnectionFactory;
import br.com.cana.util.DateUtil;

public class PartidaDAO {

    /**
     * Salva a partida, a arbitragem e todos os vínculos de jogadores 
     * em uma única transação atômica e segura.
     */
    public boolean salvarCompleto(Partida p) {
        
        String sqlPartida = "INSERT INTO Partida (dataPartida, golsTimeAzul, golsTimeVermelho, sumula, "
                + "temporada_id, nomePartida, arbitro, bandeira1, bandeira2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String sqlJogadores = "INSERT INTO JogadorPartida (partida_id, jogador_id, time, status, funcao, gols, cartao_amarelo, cartao_vermelho) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false); // 1. INICIA A TRANSAÇÃO MANUAL

            // SALVA A PARTIDA E ARBITRAGEM, RECUPERANDO O ID GERADO PARA USAR NOS VÍNCULOS
            int idPartidaGerado = 0;
            try (PreparedStatement stmtP = conn.prepareStatement(sqlPartida, Statement.RETURN_GENERATED_KEYS)) {
                stmtP.setString(1, DateUtil.paraBanco(p.getDataPartida()));
                stmtP.setInt(2, p.getGolsTimeAzul());
                stmtP.setInt(3, p.getGolsTimeVermelho());
                stmtP.setString(4, p.getSumula());
                stmtP.setInt(5, p.getTemporadaId());
                stmtP.setString(6, p.getNomePartida());
                
                // Grava a equipe de arbitragem que atuou na partida
                stmtP.setString(7, p.getArbitro());
                stmtP.setString(8, p.getBandeira1());
                stmtP.setString(9, p.getBandeira2());
                
                stmtP.executeUpdate();

                ResultSet rs = stmtP.getGeneratedKeys();
                if (rs.next()) {
                    idPartidaGerado = rs.getInt(1);
                    p.setId(idPartidaGerado);
                }
            }

            // SALVA OS VÍNCULOS DOS JOGADORES (Batch para alta performance)
            try (PreparedStatement stmtJ = conn.prepareStatement(sqlJogadores)) {
                for (JogadorPartida jp : p.getListaGeralPresenca()) {
                    stmtJ.setInt(1, idPartidaGerado);
                    stmtJ.setInt(2, jp.getJogador().getId());
                    stmtJ.setString(3, jp.getTime());
                    stmtJ.setString(4, jp.getStatus());
                    stmtJ.setString(5, jp.getFuncao());
                    stmtJ.setInt(6, jp.getGols());
                    stmtJ.setInt(7, jp.getCartaoAmarelo());
                    stmtJ.setInt(8, jp.getCartaoVermelho());
                    
                    stmtJ.addBatch(); // Adiciona ao lote de execução
                }
                stmtJ.executeBatch(); // Descarrega tudo de uma vez só no banco
            }

            conn.commit(); // 4. CONFIRMA TUDO SE CHEGOU ATÉ AQUI SEM ERROS
            System.out.println("✅ Partida, arbitragem, súmula e presença salvos com sucesso!");
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // CANCELA TODA A OPERAÇÃO SE DER ALGO ERRADO
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } 
            }
            System.err.println("❌ Erro ao salvar partida completa: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<Partida> listarPorTemporada(int temporadaId) {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT * FROM Partida WHERE temporada_id = ? ORDER BY id DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, temporadaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Partida p = new Partida();
                p.setId(rs.getInt("id"));
                p.setTemporadaId(rs.getInt("temporada_id"));
                p.setGolsTimeAzul(rs.getInt("golsTimeAzul"));
                p.setGolsTimeVermelho(rs.getInt("golsTimevermelho"));
                p.setSumula(rs.getString("sumula"));

                // Recupera a arbitragem salva
                p.setArbitro(rs.getString("arbitro"));
                p.setBandeira1(rs.getString("bandeira1"));
                p.setBandeira2(rs.getString("bandeira2"));

                String dataStr = rs.getString("dataPartida"); 
                if (dataStr != null) {
                    p.setDataPartida(LocalDate.parse(dataStr));
                }

                p.setNomePartida(rs.getString("nomePartida"));
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao listar partidas da temporada: " + e.getMessage());
        }
        return lista;
    }
}
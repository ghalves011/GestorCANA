package br.com.cana.dao;

import br.com.cana.model.Jogador;
import br.com.cana.model.JogadorPartida;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.cana.util.ConnectionFactory;

public class JogadorPartidaDAO {

    // Vincular inicial (Todo mundo entra pro racha com 0 gols e 0 cartões)
    public boolean vincular(int jogadorId, int partidaId, String time, String status, String funcao) {
        String sql = "INSERT INTO JogadorPartida (jogador_id, partida_id, time, status, funcao, gols, cartao_amarelo, cartao_vermelho) VALUES (?, ?, ?, ?, ?, 0, 0, 0)";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jogadorId);
            stmt.setInt(2, partidaId);
            stmt.setString(3, time);
            stmt.setString(4, status);
            stmt.setString(5, funcao);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erro ao vincular jogador à partida: " + e.getMessage());
            return false;
        }
    }

    // Atualizar o vínculo (gols, cartões, função, etc)
    public boolean atualizarVinculo(JogadorPartida jp) {
        String sql = "UPDATE JogadorPartida SET time = ?, status = ?, funcao = ?, gols = ?, cartao_amarelo = ?, cartao_vermelho = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, jp.getTime());
            stmt.setString(2, jp.getStatus());
            stmt.setString(3, jp.getFuncao());
            stmt.setInt(4, jp.getGols());
            stmt.setInt(5, jp.getCartaoAmarelo()); // 👈 Enviando amarelo do Model
            stmt.setInt(6, jp.getCartaoVermelho()); // 👈 Enviando vermelho do Model
            stmt.setInt(7, jp.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erro ao atualizar vínculo do jogador: " + e.getMessage());
            return false;
        }
    }

    // Buscar os jogadores de uma partida específica, trazendo também os dados do jogador
    public List<JogadorPartida> buscarPorPartida(int partidaId) {
        List<JogadorPartida> lista = new ArrayList<>();

        // 🌟 ALTERAÇÃO: Tiramos o "jp.id AS id_relacao" da Query
        String sql = "SELECT jp.*, j.nome, j.apelido, j.posicao, j.numCamisa "
                + "FROM JogadorPartida jp "
                + "JOIN Jogador j ON jp.jogador_id = j.id "
                + "WHERE jp.partida_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, partidaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Jogador j = new Jogador();
                j.setId(rs.getInt("jogador_id"));
                j.setNome(rs.getString("nome"));
                j.setApelido(rs.getString("apelido"));
                j.setNumCamisa(rs.getInt("numCamisa"));
                j.setPosicao(rs.getString("posicao"));

                JogadorPartida jp = new JogadorPartida();
                // 🌟 Comentado pois a tabela não possui id próprio, o que importa são os dados
                // abaixo:
                // jp.setId(rs.getInt("id_relacao"));

                jp.setJogador(j);
                jp.setTime(rs.getString("time"));
                jp.setStatus(rs.getString("status"));
                jp.setFuncao(rs.getString("funcao"));
                jp.setGols(rs.getInt("gols"));
                jp.setCartaoAmarelo(rs.getInt("cartao_amarelo"));
                jp.setCartaoVermelho(rs.getInt("cartao_vermelho"));

                lista.add(jp);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar jogadores da partida: " + e.getMessage());
        }
        return lista;
    }

    // Buscar estatísticas gerais da temporada (gols, cartões, partidas jogadas) para cada jogador
    public List<Object[]> buscarEstatisticasGeraisDaTemporada(int temporadaId) {
        List<Object[]> estatisticas = new ArrayList<>();

        // Agrupa pelos dados do jogador e conta quantas partidas distintas ele
        // participou
        String sql = "SELECT j.apelido, j.nome, "
                + "SUM(jp.gols) AS total_gols, "
                + "SUM(jp.cartao_amarelo) AS total_ca, "
                + "SUM(jp.cartao_vermelho) AS total_cv, "
                + "COUNT(DISTINCT jp.partida_id) AS partidas_jogadas "
                + "FROM JogadorPartida jp "
                + "JOIN Jogador j ON jp.jogador_id = j.id "
                + "JOIN Partida p ON jp.partida_id = p.id "
                + "WHERE p.temporada_id = ? " // Ajuste o nome da coluna se for diferente no seu banco
                + "GROUP BY j.id";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, temporadaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String apelido = rs.getString("apelido");
                String nome = rs.getString("nome");
                // Garante que o apelido apareça, se não tiver, usa o nome
                String nomeExibicao = (apelido != null && !apelido.trim().isEmpty()) ? apelido : nome;

                int gols = rs.getInt("total_gols");
                int ca = rs.getInt("total_ca");
                int cv = rs.getInt("total_cv");
                int partidasJogadas = rs.getInt("partidas_jogadas");

                // Monta um array provisório com os dados crus (a % será calculada no Service)
                estatisticas.add(new Object[] { nomeExibicao, gols, ca, cv, partidasJogadas });
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar estatísticas da temporada: " + e.getMessage());
        }
        return estatisticas;
    }

    public void zerarCartoesDoJogador(int jogadorId) {
        // Zera os cartões amarelos e vermelhos do jogador em todas as partidas
        String sql = "UPDATE JogadorPartida SET cartao_amarelo = 0, cartao_vermelho = 0 WHERE jogador_id = ?";

        try (Connection conn = br.com.cana.util.ConnectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jogadorId);
            int linhasAfetadas = ps.executeUpdate(); // Executa a ação

            System.out.println("✅ Histórico de cartões zerado! Partidas afetadas: " + linhasAfetadas);

        } catch (SQLException e) {
            System.err.println("❌ Erro no SQL ao zerar cartões: " + e.getMessage());
        }
    }

    /**
     * Busca no banco de dados a soma de todos os cartões amarelos ativos 
     * que o jogador tomou nas partidas anteriores.
     */
    public int contarCartao_Amarelo(int jogadorId) {
        int totalAmarelos = 0;
        
        // 🔥 CORREÇÃO: Colocamos o nome exato da coluna no seu banco (cartao_amarelo)
        String sql = "SELECT SUM(cartao_amarelo) FROM JogadorPartida WHERE jogador_id = ?";

        try (java.sql.Connection conn = br.com.cana.util.ConnectionFactory.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jogadorId);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalAmarelos = rs.getInt(1); 
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Erro ao contar cartões amarelos acumulados: " + e.getMessage());
        }
        
        return totalAmarelos;
    }
}
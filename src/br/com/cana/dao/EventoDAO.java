package br.com.cana.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import br.com.cana.util.ConnectionFactory;

import br.com.cana.model.Evento;

public class EventoDAO {

    public boolean inserir(Evento e) {
        String sql = "INSERT INTO Evento (tipo, jogador_id, partida_id, corTime) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, e.getTipo());
            stmt.setInt(2, e.getJogador().getId());
            stmt.setInt(3, e.getPartida().getId());
            stmt.setString(4, e.getCorTime());

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("❌ Erro ao registrar lance no banco: " + ex.getMessage());
            return false;
        }
    }

    public List<Evento> listarPorPartida(int partidaId) {
        List<Evento> lista = new ArrayList<>();
        String sql = "SELECT * FROM Evento WHERE partida_id = ? ORDER BY id ASC";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, partidaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Evento e = new Evento();
                e.setId(rs.getInt("id"));
                e.setTipo(rs.getString("tipo"));
                e.setCorTime(rs.getString("corTime"));
                lista.add(e);
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erro ao listar eventos da partida: " + ex.getMessage());
        }
        return lista;
    }
    
    // Método para contar quantos cartões amarelos um jogador acumulou
    public int contarAmarelosAcumulados(int jogadorId) {
        // SQL conta quantas linhas tem o tipo 'AMARELO' para aquele jogador
        String sql = "SELECT COUNT(*) FROM Evento WHERE jogador_id = ? AND tipo = 'AMARELO'";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jogadorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1); // Retorna o número total (ex: 1, 2, 3...)
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar amarelos: " + e.getMessage());
        }
        return 0;
    }
}
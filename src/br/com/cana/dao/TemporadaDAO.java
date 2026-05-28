package br.com.cana.dao;

import br.com.cana.model.Temporada;
import br.com.cana.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TemporadaDAO {

    // 1. Método para INSERIR uma nova temporada
    public boolean inserir(Temporada temporada) {
        String sql = "INSERT INTO Temporada (ano) VALUES (?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, temporada.getAno());

            int linhasAfetadas = stmt.executeUpdate();

            // Recupera o ID gerado pelo SQLite
            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        temporada.setId(rs.getInt(1));
                    }
                }
            }

            System.out.println("✅ Temporada " + temporada.getAno() + " salva com sucesso!");
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao inserir temporada: " + e.getMessage());
            return false;
        }
    }

    // 2. Método para LISTAR todas as temporadas (Ordenado por ano DESC)
    public List<Temporada> listarTodas() {
        List<Temporada> lista = new ArrayList<>();
        String sql = "SELECT * FROM Temporada ORDER BY ano DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Temporada t = new Temporada();
                t.setId(rs.getInt("id"));
                t.setAno(rs.getInt("ano"));
                lista.add(t);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro ao listar temporadas: " + e.getMessage());
        }
        return lista;
    }

    // 3. Método para BUSCAR temporada por ano
    public Temporada buscarPorAno(int ano) {
        String sql = "SELECT * FROM Temporada WHERE ano = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ano);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Temporada(rs.getInt("id"), rs.getInt("ano"));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar temporada por ano: " + e.getMessage());
        }
        return null;
    }

    // 4. Método para EXCLUIR temporada por ID
    public boolean excluir(int id) {
        String sql = "DELETE FROM Temporada WHERE id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao excluir temporada: " + e.getMessage());
            return false;
        }
    }
}
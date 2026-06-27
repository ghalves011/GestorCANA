package br.com.cana.dao;

import br.com.cana.model.Endereco;
import br.com.cana.util.ConnectionFactory;
import java.sql.*;

public class EnderecoDAO {

    public int inserir(Endereco e) {
        String sql = "INSERT INTO Endereco (logradouro, numero, complemento, bairro, cidade, estado, cep) VALUES (?,?,?,?,?,?,?)";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getLogradouro());
            ps.setString(2, e.getNumero());
            ps.setString(3, e.getComplemento());
            ps.setString(4, e.getBairro());
            ps.setString(5, e.getCidade());
            ps.setString(6, e.getEstado());
            ps.setString(7, e.getCep());

            ps.executeUpdate();

            // Recupera o ID gerado automaticamente
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1; // Retorna -1 em caso de falha na inserção
    }

    public boolean atualizar(Endereco e) {
        String sql = "UPDATE Endereco SET logradouro=?, numero=?, complemento=?, bairro=?, cidade=?, estado=?, cep=? WHERE id_endereco=?";
        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getLogradouro());
            ps.setString(2, e.getNumero());
            ps.setString(3, e.getComplemento());
            ps.setString(4, e.getBairro());
            ps.setString(5, e.getCidade());
            ps.setString(6, e.getEstado());
            ps.setString(7, e.getCep());
            ps.setInt(8, e.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Endereco buscarPorId(int id) {
        String sql = "SELECT * FROM Endereco WHERE id_endereco = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Endereco e = new Endereco();

                    e.setId(rs.getInt("id_endereco"));

                    e.setCep(rs.getString("cep"));
                    e.setLogradouro(rs.getString("logradouro"));
                    e.setNumero(rs.getString("numero"));
                    e.setComplemento(rs.getString("complemento"));
                    e.setBairro(rs.getString("bairro"));
                    e.setCidade(rs.getString("cidade"));
                    e.setEstado(rs.getString("estado"));
                    return e;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar endereço por ID: " + e.getMessage());
        }
        return null;
    }

    public boolean excluir(int idEndereco) {
        // SQL para excluir um endereço pelo ID
        String sql = "DELETE FROM Endereco WHERE id_endereco = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEndereco);

            int linhasAfetadas = ps.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException ex) {
            System.err.println("❌ Erro ao excluir endereço: " + ex.getMessage());
            return false;
        }
    }
}
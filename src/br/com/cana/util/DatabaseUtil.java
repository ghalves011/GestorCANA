package br.com.cana.util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseUtil {

    public static void main(String[] args) {
        resetarBanco();
    }

    public static void resetarBanco() {
        // Usando a sua ConnectionFactory
        try (Connection conn = ConnectionFactory.getConnection(); 
             Statement stmt = conn.createStatement()) {
            
            System.out.println("-> Limpando tabelas para o teste...");
            
            // Comandos para o SQLite
            stmt.execute("PRAGMA foreign_keys = OFF;");
            stmt.execute("DELETE FROM Evento;");
            stmt.execute("DELETE FROM JogadorPartida;");
            stmt.execute("DELETE FROM Partida;");
            stmt.execute("DELETE FROM Jogador;");
            stmt.execute("DELETE FROM Temporada;"); 
            stmt.execute("DELETE FROM Endereco;"); 
            stmt.execute("DELETE FROM contribuicao;"); 
            stmt.execute("DELETE FROM sqlite_sequence WHERE name = 'contribuicao';"); 
            stmt.execute("PRAGMA foreign_keys = ON;");
            
            System.out.println("✅ Ambiente limpo!");
        } catch (SQLException e) {
            System.err.println("❌ Erro ao resetar: " + e.getMessage());
        }
    }
}
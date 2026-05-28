package br.com.cana.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    
    private static final String URL = "jdbc:sqlite:futebol.db";

    public static Connection getConnection() {
        try {
            // Garante que o driver está carregado
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("✅ CANA: Conexão estabelecida!");
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("❌ Erro ao conectar: " + e.getMessage());
        }
    }

    // Método utilitário para fechar se você não usar o try-with-resources
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("🔌 Conexão encerrada.");
            } catch (SQLException e) {
                System.err.println("❌ Erro ao fechar: " + e.getMessage());
            }
        }
    }
}
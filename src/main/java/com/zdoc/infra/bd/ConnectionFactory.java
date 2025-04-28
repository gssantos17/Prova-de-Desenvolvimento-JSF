package com.zdoc.infra.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String URL = "jdbc:postgresql://localhost:5432/sistemafolhabd";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    // Método para obter a conexão
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver"); // Garante que o driver seja carregado
            return DriverManager.getConnection(URL, USER, PASSWORD); // Retorna uma nova conexão
        } catch (SQLException e) {
            throw new SQLException("Erro ao conectar ao banco de dados", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver do PostgreSQL não encontrado", e);
        }
    }

    // Método para fechar a conexão
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
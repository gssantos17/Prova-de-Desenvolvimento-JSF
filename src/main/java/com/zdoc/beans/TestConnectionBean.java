package com.zdoc.beans;

import com.zdoc.infra.bd.ConnectionFactory;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

@Named("testConnectionBean")
@RequestScoped
public class TestConnectionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String connectionMessage; // Propriedade para armazenar a mensagem

    public void testConnection() throws SQLException {
        Connection conn = ConnectionFactory.getConnection();
        try {
            if (conn != null && !conn.isClosed()) {
                connectionMessage = "Conexão com o banco de dados realizada com sucesso!";
            } else {
                connectionMessage = "Falha ao conectar ao banco de dados.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            connectionMessage = "Erro ao verificar a conexão: " + e.getMessage();
        }
    }

    // Getter para o JSF acessar
    public String getConnectionMessage() {
        return connectionMessage;
    }
}
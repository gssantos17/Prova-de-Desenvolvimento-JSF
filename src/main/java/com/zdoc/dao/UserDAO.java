package com.zdoc.dao;

import com.zdoc.model.UserAccount;
import com.zdoc.infra.bd.ConnectionFactory;
import jakarta.enterprise.context.RequestScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequestScoped
public class UserDAO {

    private static final String FIND_BY_USERNAME_SQL =
            "SELECT id, username, password, employee_id FROM user_account WHERE username = ?";

    private static final String INSERT_USER_ACCOUNT_SQL =
            "INSERT INTO user_account (username, password, employee_id) VALUES (?, ?, ?)";

    // Encontrar um usuário pelo nome de usuário
    public UserAccount findByUsername(String username) {
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL)) {

            // Define o parâmetro do username
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    UserAccount userAccount = new UserAccount();
                    userAccount.setId(resultSet.getLong("id"));
                    userAccount.setUsername(resultSet.getString("username"));
                    userAccount.setPassword(resultSet.getString("password"));
                    userAccount.setEmployee(null);  // Não carregamos o Employee neste método
                    return userAccount;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Salvar um UserAccount no banco de dados
    public boolean saveUserAccount(UserAccount userAccount, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_USER_ACCOUNT_SQL)) {
            statement.setString(1, userAccount.getUsername());
            statement.setString(2, userAccount.getPassword());
            statement.setLong(3, userAccount.getEmployee().getId());
            return statement.executeUpdate() > 0;
        }
    }
}
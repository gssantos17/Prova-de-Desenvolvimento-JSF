package com.zdoc.service;

import com.zdoc.infra.bd.ConnectionFactory;
import com.zdoc.model.UserAccount;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequestScoped
public class AuthService {

    private static final String COOKIE_NAME = "userAuth";

    // Autentica o usuário e cria o cookie
    public UserAccount authenticate(String username, String password, HttpServletResponse response) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            String sql = "SELECT id, username, password FROM user_account WHERE username = ?";
            System.out.println("sql: " + sql);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    if (BCrypt.checkpw(password, storedPassword)) {

                        // Cria cookie de autenticação
                        createAuthCookie(response, username);

                        UserAccount userAccount = new UserAccount();
                        userAccount.setId(rs.getLong("id"));
                        userAccount.setUsername(rs.getString("username"));
                        return userAccount;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Falha na autenticação
    }

    // Cria o cookie de autenticação
    private void createAuthCookie(HttpServletResponse response, String username) {
        Cookie cookie = new Cookie(COOKIE_NAME, username);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1 dia
        response.addCookie(cookie);
        System.out.println("Cookie de autenticação criado: " + cookie.getValue());
    }

    // Verifica se o usuário está autenticado via cookie
    public boolean isAuthenticated(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue() != null && !cookie.getValue().isEmpty();
                }
            }
        }
        return false;
    }

    // Faz o logout apagando o cookie
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0); // Expira imediatamente
        response.addCookie(cookie);
        System.out.println("Cookie de autenticação removido.");
    }
}
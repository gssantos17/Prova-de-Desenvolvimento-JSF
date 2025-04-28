package com.zdoc.dao;

import com.zdoc.model.Employee;
import com.zdoc.model.UserAccount;
import com.zdoc.infra.bd.ConnectionFactory;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
public class EmployeeDAO {

    private static final String INSERT_EMPLOYEE_SQL =
            "INSERT INTO employee (name, cpf, base_salary) VALUES (?, ?, ?)";

    private static final String SELECT_ALL_EMPLOYEES_SQL =
            "SELECT e.id, e.name, e.cpf, e.base_salary " +
                    "FROM employee e ";

    private static final String SELECT_EMPLOYEE_BY_ID_SQL =
            "SELECT e.id, e.name, e.cpf, e.base_salary " +
                    "FROM employee e WHERE e.id = ?";

    private static final String UPDATE_EMPLOYEE_SQL =
            "UPDATE employee SET name = ?, cpf = ?, base_salary = ? WHERE id = ?";

    @Inject
    private UserDAO userDAO;

    public boolean save(Employee employee) {
        Connection connection = null;
        boolean success = false;

        try {
            connection = ConnectionFactory.getConnection();
            connection.setAutoCommit(false); // Inicia transação

            // 1. Insere o Employee
            if (!insertEmployee(employee, connection)) {
                throw new SQLException("Falha ao inserir Employee");
            }

            connection.commit(); // Confirma transação
            success = true;

        } catch (SQLException e) {
            rollbackTransaction(connection);
            logError("Erro ao salvar Employee com UserAccount", e);
        } finally {
            closeConnection(connection);
        }

        return success;
    }

    // Inserir Employee no banco com conexão
    private boolean insertEmployee(Employee employee, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_EMPLOYEE_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, employee.getName());
            statement.setString(2, employee.getCpf());
            statement.setBigDecimal(3, employee.getBaseSalary());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    employee.setId(generatedKeys.getLong(1));
                    return true;
                }
            }
            return false;
        }
    }

    // Obter todos os funcionários
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_EMPLOYEES_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Employee employee = new Employee();
                employee.setId(resultSet.getLong("id"));
                employee.setName(resultSet.getString("name"));
                employee.setCpf(resultSet.getString("cpf"));
                employee.setBaseSalary(resultSet.getBigDecimal("base_salary"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    // Selecionar um funcionário específico por ID
    public Employee getEmployeeById(Long employeeId) {
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_EMPLOYEE_BY_ID_SQL)) {
            statement.setLong(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Employee employee = new Employee();
                    employee.setId(resultSet.getLong("id"));
                    employee.setName(resultSet.getString("name"));
                    employee.setCpf(resultSet.getString("cpf"));
                    employee.setBaseSalary(resultSet.getBigDecimal("base_salary"));
                    return employee;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateEmployee(Employee employee) {
        Connection connection = null;
        boolean success = false;

        try {
            connection = ConnectionFactory.getConnection();
            connection.setAutoCommit(false); // Inicia transação

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_EMPLOYEE_SQL)) {
                statement.setString(1, employee.getName());
                statement.setString(2, employee.getCpf());
                statement.setBigDecimal(3, employee.getBaseSalary());
                statement.setLong(4, employee.getId());

                int affectedRows = statement.executeUpdate();
                success = affectedRows > 0;

                if (success) {
                    connection.commit(); // Confirma transação se bem-sucedido
                } else {
                    connection.rollback(); // Reverte se nenhuma linha foi afetada
                }
            }
        } catch (SQLException e) {
            rollbackTransaction(connection);
            logError("Erro ao atualizar Employee", e);
            success = false;
        } finally {
            closeConnection(connection);
        }

        return success;
    }

    private void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logError("Erro ao fazer rollback", e);
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true); // Restaura padrão
                connection.close();
            } catch (SQLException e) {
                logError("Erro ao fechar conexão", e);
            }
        }
    }

    private void logError(String message, Exception e) {
        // Substitua por seu sistema de log preferido
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
}
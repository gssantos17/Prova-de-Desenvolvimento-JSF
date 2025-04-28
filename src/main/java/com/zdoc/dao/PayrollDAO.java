package com.zdoc.dao;

import com.zdoc.model.Payroll;
import com.zdoc.infra.bd.ConnectionFactory;
import jakarta.enterprise.context.RequestScoped;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
public class PayrollDAO {

    private static final String INSERT_PAYROLL_SQL =
            "INSERT INTO payroll (employee_id, month_year, base_salary, other_benefits, discounts, irrf, net_value) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String CALCULATE_PAYROLL_SQL =
            "SELECT e.id, e.name, e.base_salary, " +
                    "calculate_irrf(e.base_salary, ?) as irrf, " +
                    "(e.base_salary + ?) - calculate_irrf(e.base_salary, ?) as net_value " +
                    "FROM employee e WHERE e.id = ?";

    private static final String SELECT_PAYROLLS_BY_EMPLOYEE_SQL =
            "SELECT p.*, e.name FROM payroll p " +
                    "JOIN employee e ON p.employee_id = e.id " +
                    "WHERE p.employee_id = ? ORDER BY p.month_year DESC";

    private static final String SELECT_PAYROLL_BY_ID_SQL =
            "SELECT p.*, e.name FROM payroll p " +
                    "JOIN employee e ON p.employee_id = e.id " +
                    "WHERE p.id = ?";

    public boolean savePayroll(Payroll payroll) {
        Connection connection = null;
        boolean success = false;

        try {
            connection = ConnectionFactory.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(INSERT_PAYROLL_SQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, payroll.getEmployeeId());
                statement.setString(2, payroll.getMonthYear());
                statement.setBigDecimal(3, payroll.getBaseSalary());
                statement.setBigDecimal(4, payroll.getOtherBenefits());
                statement.setBigDecimal(5, payroll.getDiscounts());
                statement.setBigDecimal(6, payroll.getIrrf());
                statement.setBigDecimal(7, payroll.getNetValue());

                int affectedRows = statement.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            payroll.setId(generatedKeys.getInt(1));
                        }
                    }
                    connection.commit();
                    success = true;
                }
            }
        } catch (SQLException e) {
            rollbackTransaction(connection);
            logError("Erro ao salvar folha de pagamento", e);
        } finally {
            closeConnection(connection);
        }

        return success;
    }

    public Payroll calculatePayroll(int employeeId, BigDecimal otherBenefits, String monthYear) {
        Payroll payroll = new Payroll();
        Connection connection = null;

        try {
            connection = ConnectionFactory.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(CALCULATE_PAYROLL_SQL)) {
                statement.setBigDecimal(1, otherBenefits);
                statement.setBigDecimal(2, otherBenefits);
                statement.setBigDecimal(3, otherBenefits);
                statement.setInt(4, employeeId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        payroll.setEmployeeId(rs.getInt("id"));
                        payroll.setEmployeeName(rs.getString("name"));
                        payroll.setMonthYear(monthYear);
                        payroll.setBaseSalary(rs.getBigDecimal("base_salary"));
                        payroll.setOtherBenefits(otherBenefits);
                        payroll.setDiscounts(BigDecimal.ZERO); // Pode ser ajustado depois
                        payroll.setIrrf(rs.getBigDecimal("irrf"));
                        payroll.setNetValue(rs.getBigDecimal("net_value"));
                    }
                }
            }
        } catch (SQLException e) {
            logError("Erro ao calcular folha de pagamento", e);
        } finally {
            closeConnection(connection);
        }

        return payroll;
    }

    public List<Payroll> getPayrollsByEmployee(int employeeId) {
        List<Payroll> payrolls = new ArrayList<>();
        Connection connection = null;

        try {
            connection = ConnectionFactory.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_PAYROLLS_BY_EMPLOYEE_SQL)) {
                statement.setInt(1, employeeId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Payroll payroll = mapResultSetToPayroll(rs);
                        payrolls.add(payroll);
                    }
                }
            }
        } catch (SQLException e) {
            logError("Erro ao buscar folhas de pagamento", e);
        } finally {
            closeConnection(connection);
        }

        return payrolls;
    }

    public Payroll getPayrollById(int payrollId) {
        Payroll payroll = null;
        Connection connection = null;

        try {
            connection = ConnectionFactory.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_PAYROLL_BY_ID_SQL)) {
                statement.setInt(1, payrollId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        payroll = mapResultSetToPayroll(rs);
                    }
                }
            }
        } catch (SQLException e) {
            logError("Erro ao buscar folha de pagamento por ID", e);
        } finally {
            closeConnection(connection);
        }

        return payroll;
    }

    private Payroll mapResultSetToPayroll(ResultSet rs) throws SQLException {
        Payroll payroll = new Payroll();
        payroll.setId(rs.getInt("id"));
        payroll.setEmployeeId(rs.getInt("employee_id"));
        payroll.setEmployeeName(rs.getString("name"));
        payroll.setMonthYear(rs.getString("month_year"));
        payroll.setBaseSalary(rs.getBigDecimal("base_salary"));
        payroll.setOtherBenefits(rs.getBigDecimal("other_benefits"));
        payroll.setDiscounts(rs.getBigDecimal("discounts"));
        payroll.setIrrf(rs.getBigDecimal("irrf"));
        payroll.setNetValue(rs.getBigDecimal("net_value"));
        payroll.setCreatedAt(rs.getDate("created_at").toLocalDate());
        return payroll;
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
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                logError("Erro ao fechar conexão", e);
            }
        }
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
}
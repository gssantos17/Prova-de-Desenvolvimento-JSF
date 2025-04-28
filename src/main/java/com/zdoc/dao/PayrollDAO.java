package com.zdoc.dao;

import com.zdoc.model.Employee;
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

    private static final String SELECT_ALL_PAYROLLS_SQL =
            "SELECT p.*, e.name FROM payroll p " +
                    "JOIN employee e ON p.employee_id = e.id " +
                    "ORDER BY p.month_year DESC, e.name";

    private static final String CALCULATE_IRRF_SQL =
            "SELECT calculate_irrf(?, ?) as irrf_value";

    // Nova consulta para atualizar apenas o IRRF
    private static final String UPDATE_IRRF_SQL =
            "UPDATE payroll SET irrf = ?, net_value = (base_salary + other_benefits - discounts - ?) WHERE id = ?";


    public boolean savePayroll(Payroll payroll) {
        Connection connection = null;
        boolean success = false;

        try {
            connection = ConnectionFactory.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(INSERT_PAYROLL_SQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, payroll.getEmployee().getId());
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
                            payroll.setId(generatedKeys.getLong(1));
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

    public BigDecimal calculateIrrfValue(BigDecimal baseSalary, BigDecimal otherBenefits) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CALCULATE_IRRF_SQL)) {

            stmt.setBigDecimal(1, baseSalary);
            stmt.setBigDecimal(2, otherBenefits);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("irrf_value");
                }
            }
        } catch (SQLException e) {
            logError("Erro ao calcular IRRF", e);
        }
        return BigDecimal.ZERO;
    }

    // Método para atualizar apenas o IRRF e o valor líquido
    public boolean updatePayrollIrrf(Long payrollId, BigDecimal irrfValue) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_IRRF_SQL)) {

            stmt.setBigDecimal(1, irrfValue);
            stmt.setBigDecimal(2, irrfValue);
            stmt.setLong(3, payrollId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logError("Erro ao atualizar IRRF da folha", e);
            return false;
        }
    }

    public List<Payroll> getAllPayrolls() {
        List<Payroll> payrolls = new ArrayList<>();
        Connection connection = null;

        try {
            connection = ConnectionFactory.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_PAYROLLS_SQL);
                 ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    Payroll payroll = mapResultSetToPayroll(rs);
                    payrolls.add(payroll);
                }
            }
        } catch (SQLException e) {
            logError("Erro ao buscar todas as folhas de pagamento", e);
        } finally {
            closeConnection(connection);
        }

        return payrolls;
    }

    public List<Payroll> getPayrollsByEmployee(Long employeeId) {
        List<Payroll> payrolls = new ArrayList<>();
        Connection connection = null;

        try {
            connection = ConnectionFactory.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_PAYROLLS_BY_EMPLOYEE_SQL)) {
                statement.setLong(1, employeeId);

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

    public Payroll getPayrollById(Long payrollId) {
        Payroll payroll = null;
        Connection connection = null;

        try {
            connection = ConnectionFactory.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_PAYROLL_BY_ID_SQL)) {
                statement.setLong(1, payrollId);

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
        Employee employee = new Employee();
        employee.setId(rs.getLong("employee_id"));
        employee.setName(rs.getString("name"));
        Payroll payroll = new Payroll();
        payroll.setId(rs.getLong("id"));
        payroll.setEmployee(employee);
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
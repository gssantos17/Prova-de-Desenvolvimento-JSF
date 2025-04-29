package com.zdoc.dao;

import com.zdoc.infra.bd.ConnectionFactory;
import com.zdoc.model.Payroll;
import jakarta.enterprise.context.RequestScoped;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
public class ReportDAO {

    private static final String SELECT_PAYROLL_REPORT_SQL =
            "SELECT p.id, e.name AS employee_name, p.month_year, p.base_salary, " +
                    "p.other_benefits, p.discounts, p.irrf, p.net_value " +
                    "FROM payroll p " +
                    "INNER JOIN employee e ON p.employee_id = e.id";

    public List<Payroll> getPayrollReportData() {
        List<Payroll> payrollList = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_PAYROLL_REPORT_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Payroll payroll = new Payroll();
                payroll.setId(resultSet.getLong("id"));

                com.zdoc.model.Employee employee = new com.zdoc.model.Employee();
                employee.setName(resultSet.getString("employee_name"));
                payroll.setEmployee(employee);
                payroll.setEmployeeName(employee.getName());
                payroll.setMonthYear(resultSet.getString("month_year"));
                payroll.setBaseSalary(resultSet.getBigDecimal("base_salary"));
                payroll.setOtherBenefits(resultSet.getBigDecimal("other_benefits"));
                payroll.setDiscounts(resultSet.getBigDecimal("discounts"));
                payroll.setIrrf(resultSet.getBigDecimal("irrf"));
                payroll.setNetValue(resultSet.getBigDecimal("net_value"));

                payrollList.add(payroll);
            }
        } catch (SQLException e) {
            logError("Erro ao buscar dados do relatório de folhas de pagamento", e);
        }

        return payrollList;
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
}
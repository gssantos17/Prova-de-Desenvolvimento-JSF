package com.zdoc.service;

import com.zdoc.dao.PayrollDAO;
import com.zdoc.model.Payroll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class PayrollService {

    @Inject
    private PayrollDAO payrollDAO;

    public Payroll calculatePayroll(Integer employeeId, BigDecimal otherBenefits, String monthYear) {
        if (employeeId == null || monthYear == null) {
            throw new IllegalArgumentException("Employee ID e Month/Year são obrigatórios");
        }
        return payrollDAO.calculatePayroll(employeeId, otherBenefits != null ? otherBenefits : BigDecimal.ZERO, monthYear);
    }

    public boolean savePayroll(Payroll payroll) {
        if (payroll == null || payroll.getEmployeeId() == null || payroll.getMonthYear() == null) {
            throw new IllegalArgumentException("Payroll inválido");
        }
        return payrollDAO.savePayroll(payroll);
    }

    public List<Payroll> getEmployeePayrolls(Integer employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID é obrigatório");
        }
        return payrollDAO.getPayrollsByEmployee(employeeId);
    }

    public Payroll getPayrollById(Integer payrollId) {
        if (payrollId == null) {
            throw new IllegalArgumentException("Payroll ID é obrigatório");
        }
        return payrollDAO.getPayrollById(payrollId);
    }
}
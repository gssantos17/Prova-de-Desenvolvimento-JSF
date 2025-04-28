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

    // Novo método específico para cálculo de IRRF
    public BigDecimal calculateIrrfValue(BigDecimal baseSalary, BigDecimal otherBenefits) {
        if (baseSalary == null) {
            throw new IllegalArgumentException("Salário base é obrigatório");
        }
        return payrollDAO.calculateIrrfValue(
                baseSalary,
                otherBenefits != null ? otherBenefits : BigDecimal.ZERO
        );
    }

    // Método para atualizar apenas o IRRF de uma folha existente
    public boolean updatePayrollIrrf(Long payrollId, BigDecimal irrfValue) {
        if (payrollId == null || irrfValue == null) {
            throw new IllegalArgumentException("ID da folha e valor do IRRF são obrigatórios");
        }
        return payrollDAO.updatePayrollIrrf(payrollId, irrfValue);
    }

    public boolean savePayroll(Payroll payroll) {
        if (payroll == null || payroll.getEmployee().getId() == null || payroll.getMonthYear() == null) {
            throw new IllegalArgumentException("Payroll inválido");
        }
        return payrollDAO.savePayroll(payroll);
    }

    public List<Payroll> getAllPayrolls() {
        return payrollDAO.getAllPayrolls();
    }

    public List<Payroll> getEmployeePayrolls(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID é obrigatório");
        }
        return payrollDAO.getPayrollsByEmployee(employeeId);
    }

    public Payroll getPayrollById(Long payrollId) {
        if (payrollId == null) {
            throw new IllegalArgumentException("Payroll ID é obrigatório");
        }
        return payrollDAO.getPayrollById(payrollId);
    }
}
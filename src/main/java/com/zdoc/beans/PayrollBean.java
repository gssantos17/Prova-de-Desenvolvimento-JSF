package com.zdoc.beans;

import com.zdoc.model.Employee;
import com.zdoc.model.Payroll;
import com.zdoc.service.EmployeeService;
import com.zdoc.service.PayrollService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Named
@ViewScoped
public class PayrollBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Payroll payroll;
    private List<Payroll> payrollList;
    private Payroll selectedPayroll;
    private Long employeeId;
    private Employee employee;
    private List<Employee> allEmployees;  // Lista de todos os funcionários

    @Inject
    private PayrollService payrollService;

    @Inject
    private EmployeeService employeeService;

    @PostConstruct
    public void init() {
        payroll = new Payroll();

        // Carrega todos os funcionários se não foi passado um ID
        if (employeeId != null) {
            employee = employeeService.getEmployeeById(employeeId);
        } else {
            allEmployees = employeeService.getAllEmployees();  // Carregar todos os funcionários
        }

        loadAllPayrolls();
    }

    public String savePayroll() {
        try {
            if (employeeId != null) {
                employee = employeeService.getEmployeeById(employeeId);
            }
            payroll.setEmployee(employee);
            payroll.setBaseSalary(employee.getBaseSalary());

            BigDecimal netValue = payroll.getBaseSalary()
                    .add(payroll.getOtherBenefits())
                    .subtract(payroll.getDiscounts().add(payroll.getIrrf()));

            payroll.setNetValue(netValue);

            payrollService.savePayroll(payroll);
            addInfoMessage("Folha salarial criada com sucesso!");
            return "/page/payroll/list_payroll.xhtml?faces-redirect=true";
        } catch (Exception e) {
            addErrorMessage("Erro ao criar folha: " + e.getMessage());
            return null;
        }
    }

    public void calculateIrrf() {
        BigDecimal salary = employee.getBaseSalary();
        BigDecimal outrosProventos = payroll.getOtherBenefits() != null ? payroll.getOtherBenefits() : BigDecimal.ZERO;
        BigDecimal irrfValue = payrollService.calculateIrrfValue(salary, outrosProventos);

        payroll.setIrrf(irrfValue);
    }

    public void loadEmployeeData() {
        if (employeeId != null) {
            employee = employeeService.getEmployeeById(employeeId);
            payroll.setEmployee(employee);
            payroll.setBaseSalary(employee.getBaseSalary()); // Atualiza o salário base
        }
    }

    public void loadAllPayrolls() {
        try {
            if (employeeId != null) {
                payrollList = payrollService.getEmployeePayrolls(employeeId);
            } else {
                payrollList = payrollService.getAllPayrolls();
            }
        } catch (Exception e) {
            addErrorMessage("Erro ao carregar folhas: " + e.getMessage());
            payrollList = List.of();
        }
    }

    public void loadPayrollDetails(Long payrollId) {
        selectedPayroll = payrollService.getPayrollById(payrollId);
    }

    private void addInfoMessage(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", detail));
    }

    // Getters e Setters
    public Payroll getPayroll() {
        return payroll;
    }

    public void setPayroll(Payroll payroll) {
        this.payroll = payroll;
    }

    public List<Payroll> getPayrollList() {
        return payrollList;
    }

    public void setPayrollList(List<Payroll> payrollList) {
        this.payrollList = payrollList;
    }

    public Payroll getSelectedPayroll() {
        return selectedPayroll;
    }

    public void setSelectedPayroll(Payroll selectedPayroll) {
        this.selectedPayroll = selectedPayroll;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public List<Employee> getAllEmployees() {
        return allEmployees;
    }

    public void setAllEmployees(List<Employee> allEmployees) {
        this.allEmployees = allEmployees;
    }
}


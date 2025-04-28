package com.zdoc.beans;

import com.zdoc.model.Employee;
import com.zdoc.service.EmployeeService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

@Named
@RequestScoped
public class EmployeeBean implements Serializable {

    private Employee employee;
    private Employee selectedEmployee;
    private String confirmPassword;
    private String newPassword;
    private Long employeeID;

    @Inject
    private EmployeeService employeeService;

    private List<Employee> employeeList;

    @PostConstruct
    public void init() {
        employee = new Employee();
        selectedEmployee = new Employee();
    }

    public String save() {

        // Chama o serviço para salvar o funcionário e o usuário
        boolean success = employeeService.save(employee);

        if (success) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Funcionário e Usuário cadastrados com sucesso.", ""));
            return "list_employee.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha ao cadastrar funcionário e usuário.", ""));
            return null;
        }
    }

    public String update() {
        try {
            System.out.println("ID SELECTED " + selectedEmployee.getId());
            boolean updated = employeeService.updateEmployee(selectedEmployee);

            if (updated) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Funcionário atualizado com sucesso", ""));
                return "list_employee.xhtml?faces-redirect=true";
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao atualizar funcionário", "Tente novamente."));
                return null;
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao atualizar funcionário", e.getMessage()));
            return null;
        }
    }

    public List<Employee> getEmployeeList() {
        if (employeeList == null) {
            employeeList = employeeService.getAllEmployees();
        }
        return employeeList;
    }

    public void onLoad() {
        if (employeeID != null && employeeID > 0) {
            this.selectedEmployee = employeeService.getEmployeeById(employeeID);
            System.out.println("id " + this.selectedEmployee.getId());
            if (this.selectedEmployee == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Funcionário não encontrado"));
                this.selectedEmployee = new Employee();
            }
        } else {
            this.selectedEmployee = new Employee();
        }
    }

    public Employee getEmployeeById(Long id){
        return  employeeService.getEmployeeById(id);
    }

    // Getters e Setters
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Employee getSelectedEmployee() {
        return selectedEmployee;
    }

    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Long getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Long employeeID) {
        this.employeeID = employeeID;
    }
}
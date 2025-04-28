package com.zdoc.service;

import com.zdoc.model.Employee;
import com.zdoc.model.UserAccount;
import com.zdoc.dao.EmployeeDAO;
import com.zdoc.dao.UserDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.util.List;

@RequestScoped
public class EmployeeService {

    @Inject
    private EmployeeDAO employeeDAO;  // Injetando o EmployeeDAO

    public boolean saveEmployeeWithUser(Employee employee) {
        if (employee == null || employee.getUserAccount() == null) {
            throw new IllegalArgumentException("Employee e UserAccount não podem ser nulos");
        }

        return employeeDAO.saveEmployeeWithUser(employee, employee.getUserAccount());
    }

    public boolean updateEmployee(Employee employee, boolean isUpdatePassword){
        if (employee == null) {
            throw new IllegalArgumentException("Employee não podem ser nulos");
        }

        System.out.println("EMPLOYEE ID" + employee.getId());

        return employeeDAO.updateEmployee(employee);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    public Employee getEmployeeById(Long employeeId) {
        return employeeDAO.getEmployeeById(employeeId);
    }
}
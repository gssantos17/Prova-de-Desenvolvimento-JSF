package com.zdoc.service;

import com.zdoc.model.Employee;
import com.zdoc.dao.EmployeeDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@RequestScoped
public class EmployeeService {

    @Inject
    private EmployeeDAO employeeDAO;  // Injetando o EmployeeDAO

    public boolean save(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee não podem ser nulos");
        }

        return employeeDAO.save(employee);
    }

    public boolean updateEmployee(Employee employee){
        if (employee == null) {
            throw new IllegalArgumentException("Employee não podem ser nulos");
        }
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
package com.zdoc.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class TestBean {
    public String getMessage() {
        return "Funcionando!";
    }
}
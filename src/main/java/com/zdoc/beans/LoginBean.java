package com.zdoc.beans;

import com.zdoc.model.UserAccount;
import com.zdoc.service.AuthService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;

@Named
@RequestScoped
public class LoginBean {

    private String username;
    private String password;

    @Inject
    private AuthService authService;

    /**
     * Método responsável pela autenticação do usuário.
     * @return A página para redirecionamento ou null em caso de erro.
     */
    public String login() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        UserAccount userAccount = authService.authenticate(username, password, response);

        if (userAccount != null) {
            return "/page/home.xhtml?faces-redirect=true"; // Redireciona para home
        } else {
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Usuário ou senha inválidos", "Usuário ou senha inválidos"));
            return null; // Fica na mesma página de login
        }
    }

    public String logout(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        authService.logout(response);
        return "/page/login.xhtml?faces-redirect=true";
    }

    // Getters e Setters para username e password

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
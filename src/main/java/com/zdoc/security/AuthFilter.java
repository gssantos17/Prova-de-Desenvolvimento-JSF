package com.zdoc.security;

import com.zdoc.service.AuthService;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("*.xhtml")
public class AuthFilter implements Filter {

    private static final String LOGIN_PAGE = "/page/login.xhtml";

    @Inject
    private AuthService authService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();

        boolean isLoginPage = requestURI.equals(contextPath + LOGIN_PAGE);
        boolean isResourceRequest = isStaticResource(requestURI, contextPath);
        boolean isLoggedIn = authService.isAuthenticated(req);
        /*
        System.out.println("isLoginPage: " + isLoginPage);
        System.out.println("isResourceRequest: " + isResourceRequest);
        System.out.println("isLoggedIn: " + isLoggedIn); */

        if (isLoggedIn || isLoginPage || isResourceRequest) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(contextPath + LOGIN_PAGE);
        }
    }

    private boolean isStaticResource(String requestURI, String contextPath) {
        return requestURI.startsWith(contextPath + "/javax.faces.resource/")
                || requestURI.startsWith(contextPath + "/resources/")
                || requestURI.endsWith(".css")
                || requestURI.endsWith(".js")
                || requestURI.endsWith(".png")
                || requestURI.endsWith(".jpg")
                || requestURI.endsWith(".jpeg")
                || requestURI.endsWith(".gif")
                || requestURI.endsWith(".svg")
                || requestURI.endsWith(".woff")
                || requestURI.endsWith(".woff2")
                || requestURI.endsWith(".ttf")
                || requestURI.endsWith(".eot")
                || requestURI.endsWith("/favicon.ico");
    }
}
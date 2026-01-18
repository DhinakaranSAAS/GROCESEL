package com.groseloa.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // Allow public endpoints
        if (path.startsWith("/api/auth/") || path.startsWith("/h2-console")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check session for other API endpoints
        if (path.startsWith("/api/")) {
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("Unauthorized");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}

package com.freshmart.web.filter;

import com.freshmart.entity.User;
import com.freshmart.enums.Role;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/admin/*"})
public class AdminOnlyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        Object obj = request.getSession().getAttribute("authUser");
        if (obj == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) obj;
        if (user.getRole() != Role.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/catalog");
            return;
        }

        chain.doFilter(req, resp);
    }
}
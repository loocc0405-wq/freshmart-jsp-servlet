package com.freshmart.web.filter;

import com.freshmart.entity.User;
import com.freshmart.enums.Role;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/staff/*"})
public class StaffOnlyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        Object obj = request.getSession().getAttribute("authUser");

        // chưa login -> đưa về login
        if (obj == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) obj;

        // không phải STAFF -> đưa về catalog (hoặc 403)
        if (user.getRole() != Role.STAFF) {
            response.sendRedirect(request.getContextPath() + "/catalog");
            return;
        }

        chain.doFilter(req, resp);
    }
}
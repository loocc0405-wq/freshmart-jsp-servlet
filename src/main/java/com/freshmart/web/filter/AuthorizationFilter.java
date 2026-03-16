package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.util.WebUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Authorization filter: checks role vs URL prefix.
 *
 * Example rules:
 * - /admin/*  => ADMIN
 * - /staff/*  => STAFF or ADMIN
 * - /seller/* => SELLER or ADMIN
 */
public class AuthorizationFilter implements Filter {

    private final Map<String, Set<Role>> rules = new LinkedHashMap<>();

    @Override
    public void init(FilterConfig filterConfig) {
        rules.put("/admin", EnumSet.of(Role.ADMIN));
        rules.put("/staff", EnumSet.of(Role.STAFF, Role.ADMIN));
        rules.put("/seller", EnumSet.of(Role.SELLER, Role.ADMIN));
        rules.put("/pro", EnumSet.of(Role.CUSTOMER, Role.STAFF, Role.ADMIN));
        rules.put("/customer", EnumSet.of(Role.CUSTOMER, Role.ADMIN));
        rules.put("/subscription", EnumSet.of(Role.CUSTOMER, Role.ADMIN));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        User u = (User) request.getSession().getAttribute(AppConstants.SESSION_USER);
        if (u == null) {
            // AuthenticationFilter should catch, but keep it safe.
            response.sendRedirect(WebUtil.contextPath(request) + "/login");
            return;
        }

        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        String path = uri.substring(ctx.length()); // "/seller/pos"

        for (Map.Entry<String, Set<Role>> e : rules.entrySet()) {
            String rulePath = e.getKey();
            if (path.equals(rulePath) || path.startsWith(rulePath + "/")) {
                if (!e.getValue().contains(u.getRole())) {
                    response.setStatus(403);
                    request.setAttribute("errorMessage", "Bạn không có quyền truy cập trang này.");
                    request.getRequestDispatcher("/WEB-INF/jsp/common/403.jsp").forward(request, response);
                    return;
                }
                break;
            }
        }

        chain.doFilter(req, resp);
    }
}

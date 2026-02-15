package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.util.WebUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        rules.put("/admin/", EnumSet.of(Role.ADMIN));
        rules.put("/staff/", EnumSet.of(Role.STAFF, Role.ADMIN));
        rules.put("/seller/", EnumSet.of(Role.SELLER, Role.ADMIN));
        rules.put("/pro/", EnumSet.of(Role.CUSTOMER, Role.STAFF, Role.ADMIN)); // dashboard/forecast
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
            if (path.startsWith(e.getKey())) {
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

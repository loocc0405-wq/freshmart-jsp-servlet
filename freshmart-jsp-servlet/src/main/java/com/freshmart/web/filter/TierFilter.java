package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.util.WebUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Tier filter: gate PRO module for customers with active PRO tier.
 * Applies to /pro/*
 */
public class TierFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        User u = (User) request.getSession().getAttribute(AppConstants.SESSION_USER);
        if (u == null) {
            response.sendRedirect(WebUtil.contextPath(request) + "/login");
            return;
        }

        // Only gate customers; staff/admin can view dashboard for oversight
        if (u.getRole() == Role.CUSTOMER) {
            boolean ok = u.isProActive(LocalDate.now());
            if (!ok) {
                response.sendRedirect(WebUtil.contextPath(request) + "/subscription/upgrade");
                return;
            }
        }

        chain.doFilter(req, resp);
    }
}

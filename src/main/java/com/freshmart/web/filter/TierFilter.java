package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.service.SubscriptionService;
import com.freshmart.util.WebUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

public class TierFilter implements Filter {

    private final SubscriptionService subscriptionService = new SubscriptionService();

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

        if (u.getRole() == Role.CUSTOMER) {
            User fresh = subscriptionService.refreshAndSync(u.getId());
            request.getSession().setAttribute(AppConstants.SESSION_USER, fresh);

            boolean ok = fresh.isProActive(LocalDate.now());
            if (!ok) {
                response.sendRedirect(WebUtil.contextPath(request) + "/subscription/upgrade");
                return;
            }
        }

        chain.doFilter(req, resp);
    }
}
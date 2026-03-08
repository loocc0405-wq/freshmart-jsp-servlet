package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.service.SubscriptionService;
import com.freshmart.service.dto.SubscriptionStatusDTO;
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
            // refreshAndSync now also records EXPIRE tier history if PRO expired
            User fresh = subscriptionService.refreshAndSync(u.getId());
            request.getSession().setAttribute(AppConstants.SESSION_USER, fresh);

            boolean ok = fresh.isProActive(LocalDate.now());
            if (!ok) {
                // Compute status to check if user is still within grace period
                SubscriptionStatusDTO status = subscriptionService.computeStatus(fresh);
                String redirect = WebUtil.contextPath(request)
                        + "/subscription/upgrade?expired=1";
                if (status.isExpiredInGrace()) {
                    redirect += "&grace=1";
                }
                response.sendRedirect(redirect);
                return;
            }
        }

        chain.doFilter(req, resp);
    }
}
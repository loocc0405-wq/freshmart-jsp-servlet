package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.SubscriptionService;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthenticationFilter implements Filter {

    private final SubscriptionService subscriptionService = new SubscriptionService();

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession(false);

        User sessionUser = null;
        if (session != null) {
            Object raw = session.getAttribute(AppConstants.SESSION_USER);
            if (raw instanceof User) {
                sessionUser = (User) raw;
            }
        }

        if (sessionUser == null) {
            String uri = request.getRequestURI();
            String query = request.getQueryString();

            String returnUrl = uri;
            if (query != null && !query.isBlank()) {
                returnUrl += "?" + query;
            }

            String encoded = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login?return=" + encoded);
            return;
        }

        try {
            User fresh = subscriptionService.refreshAndSync(sessionUser.getId());
            request.getSession().setAttribute(AppConstants.SESSION_USER, fresh);
        } catch (RuntimeException ex) {
            request.getSession().invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        chain.doFilter(req, resp);
    }
}
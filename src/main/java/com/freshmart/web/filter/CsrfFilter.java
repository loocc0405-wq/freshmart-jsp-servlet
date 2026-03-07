package com.freshmart.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN = "CSRF_TOKEN";
    private static final String CSRF_PARAM = "csrf_token";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(true);

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Bỏ qua static resources
        if (uri.contains("/css/") ||
            uri.contains("/js/") ||
            uri.contains("/images/") ||
            uri.contains("/fonts/")) {

            chain.doFilter(req, res);
            return;
        }

        // ===== GET: tạo CSRF token nếu chưa có =====
        // previously token was regenerated on every GET which could
        // invalidate forms when the user navigated or refreshed before
        // submitting. Only create once per session here.
        if ("GET".equalsIgnoreCase(method)) {
            if (session.getAttribute(CSRF_TOKEN) == null) {
                String token = UUID.randomUUID().toString();
                session.setAttribute(CSRF_TOKEN, token);
            }
        }

        // ===== POST: kiểm tra CSRF token =====
        if ("POST".equalsIgnoreCase(method)) {

            String sessionToken = (String) session.getAttribute(CSRF_TOKEN);
            String requestToken = request.getParameter(CSRF_PARAM);

            if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "CSRF token invalid"
                );
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
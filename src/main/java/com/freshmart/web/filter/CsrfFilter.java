package com.freshmart.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN = "CSRF_TOKEN";
    private static final String CSRF_PARAM = "csrf_token";
    private static final String CSRF_HEADER = "X-CSRF-Token";

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

        // Luôn đảm bảo token tồn tại
        if (session.getAttribute(CSRF_TOKEN) == null) {
            String token = UUID.randomUUID().toString();
            session.setAttribute(CSRF_TOKEN, token);
        }

        // Kiểm tra CSRF cho POST
        if ("POST".equalsIgnoreCase(method)) {

            String sessionToken = (String) session.getAttribute(CSRF_TOKEN);

            // Ưu tiên lấy từ form parameter
            String requestToken = request.getParameter(CSRF_PARAM);

            // Nếu không có thì lấy từ header (dùng cho fetch/AJAX/JSON)
            if (requestToken == null || requestToken.isBlank()) {
                requestToken = request.getHeader(CSRF_HEADER);
            }

            if (sessionToken == null ||
                requestToken == null ||
                !sessionToken.equals(requestToken)) {

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
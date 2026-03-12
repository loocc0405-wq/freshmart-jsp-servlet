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

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Static resources: skip entirely – do NOT create a session
        if (isStaticResource(uri)) {
            chain.doFilter(req, res);
            return;
        }

        // Only now create / retrieve session (after bypassing statics)
        HttpSession session = request.getSession(true);

        if (session.getAttribute(CSRF_TOKEN) == null) {
            session.setAttribute(CSRF_TOKEN, UUID.randomUUID().toString());
        }

        if (isUnsafeMethod(method)) {
            String sessionToken = (String) session.getAttribute(CSRF_TOKEN);
            String requestToken = request.getParameter(CSRF_PARAM);

            if (requestToken == null || requestToken.isBlank()) {
                requestToken = request.getHeader(CSRF_HEADER);
            }

            if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private boolean isUnsafeMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private boolean isStaticResource(String uri) {
        return uri.contains("/css/")
                || uri.contains("/js/")
                || uri.contains("/images/")
                || uri.contains("/fonts/");
    }
}
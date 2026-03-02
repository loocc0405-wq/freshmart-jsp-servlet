package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Authentication filter: requires login for protected routes.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        Object user = request.getSession().getAttribute(AppConstants.SESSION_USER);

        if (user == null) {

            // ✅ Lấy path nội bộ (KHÔNG dùng full URL)
            String uri = request.getRequestURI();       // ví dụ: /FreshMart/admin
            String query = request.getQueryString();    // ví dụ: id=5

            String returnUrl = uri;

            if (query != null && !query.isBlank()) {
                returnUrl += "?" + query;
            }

            // Encode an toàn
            String encoded = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);

            response.sendRedirect(request.getContextPath() + "/login?return=" + encoded);
            return;
        }

        chain.doFilter(req, resp);
    }
}
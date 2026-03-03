package com.freshmart.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession();

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String token = UUID.randomUUID().toString();
            session.setAttribute("CSRF_TOKEN", token);
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String sessionToken = (String) session.getAttribute("CSRF_TOKEN");
            String requestToken = request.getParameter("csrf_token");

            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "CSRF token invalid");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
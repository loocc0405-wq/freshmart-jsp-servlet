package com.freshmart.web.servlet;

import com.freshmart.util.WebUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

@WebServlet(urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private static final String CSRF_SESSION_KEY = "CSRF_TOKEN";

    /**
     * Show a confirmation page that submits a POST request (safer than GET logout).
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(WebUtil.contextPath(req) + "/login");
            return;
        }

        // Generate CSRF token for logout form (self-contained; not relying on CsrfFilter mapping)
        String token = UUID.randomUUID().toString();
        session.setAttribute(CSRF_SESSION_KEY, token);
        req.setAttribute("csrfToken", token);

        req.getRequestDispatcher("/WEB-INF/jsp/common/logout_confirm.jsp").forward(req, resp);
    }

    /**
     * Perform logout (POST only).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(WebUtil.contextPath(req) + "/login");
            return;
        }

        String sessionToken = (String) session.getAttribute(CSRF_SESSION_KEY);
        String requestToken = req.getParameter("csrf_token");
        if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
            return;
        }

        session.invalidate();
        resp.sendRedirect(WebUtil.contextPath(req) + "/login?logout=1");
    }
}
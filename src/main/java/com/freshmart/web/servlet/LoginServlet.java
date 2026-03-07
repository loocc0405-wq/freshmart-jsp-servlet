package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.exception.AuthenticationException;
import com.freshmart.security.LoginAttemptService;
import com.freshmart.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private static final LoginAttemptService attemptService = new LoginAttemptService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (user != null) {
            redirectByRole(user, req, resp);
            return;
        }

        req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String returnUrl = req.getParameter("return");

        if (attemptService.isBlocked(username)) {
            req.setAttribute("error",
                    "Tài khoản bị khóa tạm thời do nhập sai quá 5 lần. Vui lòng thử lại sau 5 phút.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = authService.login(username, password);
            attemptService.loginSuccess(username);

            req.getSession().setAttribute(AppConstants.SESSION_USER, user);

            // Ensure CSRF token exists for later POST actions (e.g., logout)
            jakarta.servlet.http.HttpSession session = req.getSession();
            if (session.getAttribute("CSRF_TOKEN") == null) {
                session.setAttribute("CSRF_TOKEN", java.util.UUID.randomUUID().toString());
            }

            String contextPath = req.getContextPath();

            if (returnUrl != null && !returnUrl.isBlank() && !returnUrl.startsWith("http")) {
                if (returnUrl.startsWith(contextPath)) {
                    resp.sendRedirect(returnUrl);
                } else {
                    resp.sendRedirect(contextPath + returnUrl);
                }
                return;
            }

            redirectByRole(user, req, resp);

        } catch (AuthenticationException ex) {
            attemptService.loginFailed(username);
            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
        }
    }

    private void redirectByRole(User user, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String contextPath = req.getContextPath();

        switch (user.getRole()) {
            case ADMIN:
                resp.sendRedirect(contextPath + "/admin/sellers");
                break;

            case STAFF:
                resp.sendRedirect(contextPath + "/staff/forecast");
                break;

            case SELLER:
                resp.sendRedirect(contextPath + "/seller/pos");
                break;

            case CUSTOMER:
                resp.sendRedirect(contextPath + "/customer/dashboard");
                break;

            default:
                resp.sendRedirect(contextPath + "/catalog");
        }
    }
}
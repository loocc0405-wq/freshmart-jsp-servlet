package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.exception.AuthenticationException;
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

    @Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    Object sessUser = req.getSession().getAttribute(AppConstants.SESSION_USER);

    // Nếu đã login rồi
    if (sessUser != null) {
        String returnUrl = req.getParameter("return");
        String contextPath = req.getContextPath();

        // Nếu có return hợp lệ -> quay lại
        if (returnUrl != null && !returnUrl.isBlank() && !returnUrl.startsWith("http")) {
            if (returnUrl.startsWith(contextPath)) {
                resp.sendRedirect(returnUrl);
            } else {
                resp.sendRedirect(contextPath + returnUrl);
            }
            return;
        }

        // Không có return -> điều hướng theo role
        User user = (User) sessUser;
        String role = user.getRole().name();

            switch (role) {
                case "ADMIN":
                    resp.sendRedirect(contextPath + "/admin");
                    break;

                case "STAFF":
                    resp.sendRedirect(contextPath + "/staff/suppliers");
                    break;

                case "SELLER":
                    resp.sendRedirect(contextPath + "/seller/pos");
                    break;

                case "CUSTOMER":
                    resp.sendRedirect(contextPath + "/customer");
                    break;

                default:
                    resp.sendRedirect(contextPath + "/catalog");
            }
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

        try {
            User user = authService.login(username, password);

            // ✅ Lưu user vào session
            req.getSession().setAttribute(AppConstants.SESSION_USER, user);

            String contextPath = req.getContextPath();

            // ===============================
            // 1️⃣ Nếu có returnUrl hợp lệ → quay lại trang đó
            // ===============================
            if (returnUrl != null && !returnUrl.isBlank()) {

                // Chống open redirect (không cho redirect ra ngoài domain)
                if (!returnUrl.startsWith("http")) {

                    if (returnUrl.startsWith(contextPath)) {
                        resp.sendRedirect(returnUrl);
                    } else {
                        resp.sendRedirect(contextPath + returnUrl);
                    }
                    return;
                }
            }

            // ===============================
            // 2️⃣ Nếu login trực tiếp → chuyển theo role
            // ===============================
            String role = user.getRole().name();

            switch (role) {
                case "ADMIN":
                    resp.sendRedirect(contextPath + "/admin");
                    break;

                case "STAFF":
                    resp.sendRedirect(contextPath + "/staff/suppliers");
                    break;

                case "SELLER":
                    resp.sendRedirect(contextPath + "/seller/pos");
                    break;

                case "CUSTOMER":
                    resp.sendRedirect(contextPath + "/customer");
                    break;

                default:
                    resp.sendRedirect(contextPath + "/catalog");
            }

        } catch (AuthenticationException ex) {

            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp")
                    .forward(req, resp);
        }
    }
}
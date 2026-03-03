package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.exception.AuthenticationException;
import com.freshmart.service.AuthService;
import com.freshmart.security.LoginAttemptService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    // ✅ Thêm rate-limit service
    private static final LoginAttemptService attemptService = new LoginAttemptService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getSession().getAttribute(AppConstants.SESSION_USER) != null) {
            resp.sendRedirect(req.getContextPath() + "/catalog");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String returnUrl = req.getParameter("return");

        // ===============================
        // ✅ 1️⃣ Kiểm tra tài khoản có bị khóa không
        // ===============================
        if (attemptService.isBlocked(username)) {
            req.setAttribute("error",
                    "Tài khoản bị khóa tạm thời do nhập sai quá 5 lần. Vui lòng thử lại sau 5 phút.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp")
                    .forward(req, resp);
            return;
        }

        try {
            User user = authService.login(username, password);

            // ===============================
            // ✅ 2️⃣ Nếu login thành công → reset số lần sai
            // ===============================
            attemptService.loginSuccess(username);

            req.getSession().setAttribute(AppConstants.SESSION_USER, user);

            String contextPath = req.getContextPath();

            // ===============================
            // 1️⃣ Nếu có returnUrl hợp lệ → quay lại trang đó
            // ===============================
            if (returnUrl != null && !returnUrl.isBlank()) {

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
            switch (user.getRole()) {
                case ADMIN:
                    resp.sendRedirect(contextPath + "/admin");
                    break;

                case STAFF:
                    resp.sendRedirect(contextPath + "/staff/suppliers");
                    break;

                case SELLER:
                    resp.sendRedirect(contextPath + "/seller/pos");
                    break;

                case CUSTOMER:
                    resp.sendRedirect(contextPath + "/customer");
                    break;

                default:
                    resp.sendRedirect(contextPath + "/catalog");
            }

        } catch (AuthenticationException ex) {

            // ===============================
            // ✅ 3️⃣ Nếu login thất bại → tăng số lần sai
            // ===============================
            attemptService.loginFailed(username);

            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp")
                    .forward(req, resp);
        }
    }
}
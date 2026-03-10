package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.CartItem;
import com.freshmart.entity.User;
import com.freshmart.exception.AuthenticationException;
import com.freshmart.security.LoginAttemptService;
import com.freshmart.service.AuthService;
import com.freshmart.service.CartService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private static final LoginAttemptService attemptService = new LoginAttemptService();
    private final CartService cartService = new CartService();

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

            HttpSession session = req.getSession();
            session.setAttribute(AppConstants.SESSION_USER, user);

            Object cartObj = session.getAttribute("GUEST_CART_ITEMS");
            if (cartObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<CartItem> sessionCart = (List<CartItem>) cartObj;

                if (!sessionCart.isEmpty()) {
                    cartService.mergeCart(user.getId(), sessionCart);
                    session.removeAttribute("GUEST_CART_ITEMS");
                }
            }

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
                resp.sendRedirect(contextPath + "/pro/dashboard");
                break;

            case SELLER:
                resp.sendRedirect(contextPath + "/seller/pos");
                break;

            case CUSTOMER:
                if (user.getTier() != null && "PRO".equalsIgnoreCase(user.getTier().toString())) {
                    resp.sendRedirect(contextPath + "/pro/dashboard");
                } else {
                    resp.sendRedirect(contextPath + "/customer/dashboard");
                }
                break;

            default:
                resp.sendRedirect(contextPath + "/catalog");
        }
    }
}
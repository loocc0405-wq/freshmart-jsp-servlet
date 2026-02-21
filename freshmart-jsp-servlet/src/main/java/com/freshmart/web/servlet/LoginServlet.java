package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.exception.AuthenticationException;
import com.freshmart.service.AuthService;
import com.freshmart.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String returnUrl = req.getParameter("return");

        try {
            User u = authService.login(username, password);
            req.getSession().setAttribute(AppConstants.SESSION_USER, u);

            if (returnUrl != null && !returnUrl.isBlank()) {
                resp.sendRedirect(returnUrl);
                return;
            }

            // Simple role-based landing
            switch (u.getRole()) {
                case ADMIN:
                    resp.sendRedirect(WebUtil.contextPath(req) + "/admin");
                    break;
                case STAFF:
                    resp.sendRedirect(WebUtil.contextPath(req) + "/staff");
                    break;
                case SELLER:
                    resp.sendRedirect(WebUtil.contextPath(req) + "/seller/pos");
                    break;
                default:
                    resp.sendRedirect(WebUtil.contextPath(req) + "/catalog");
            }
        } catch (AuthenticationException ex) {
            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
        }
    }
}

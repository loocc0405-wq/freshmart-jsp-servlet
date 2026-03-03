package com.freshmart.web.servlet.admin;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.util.WebUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/admin"})
public class AdminHomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Defensive auth check (in case filter mapping misses "/admin")
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(WebUtil.contextPath(req) + "/login");
            return;
        }

        Object principal = session.getAttribute(AppConstants.SESSION_USER);
        if (!(principal instanceof User)) {
            session.invalidate();
            resp.sendRedirect(WebUtil.contextPath(req) + "/login");
            return;
        }

        User user = (User) principal;
        if (user.getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "ADMIN only");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/jsp/common/admin_home.jsp").forward(req, resp);
    }
}
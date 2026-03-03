package com.freshmart.web.servlet.staff;

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

@WebServlet(urlPatterns = {"/staff"})
public class StaffHomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Defensive auth check (in case filter mapping misses "/staff")
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
        if (user.getRole() != Role.STAFF) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "STAFF only");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/jsp/common/staff_home.jsp").forward(req, resp);
    }
}
package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.SubscriptionService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/subscription/upgrade"})
public class SubscriptionUpgradeServlet extends HttpServlet {

    private final SubscriptionService subscriptionService = new SubscriptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/common/upgrade.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int days = 30;
        String plan = req.getParameter("planDays");
        try { if (plan != null) days = Integer.parseInt(plan); } catch (Exception ignored) {}

        try {
            User updated = subscriptionService.upgradePro(u.getId(), days);
            req.getSession().setAttribute(AppConstants.SESSION_USER, updated);
            resp.sendRedirect(req.getContextPath() + "/pro/dashboard");
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/common/upgrade.jsp").forward(req, resp);
        }
    }
}

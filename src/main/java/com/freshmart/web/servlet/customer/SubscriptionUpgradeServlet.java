package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.SubscriptionPayment;
import com.freshmart.entity.User;
import com.freshmart.service.SubscriptionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = { "/subscription/upgrade" })
public class SubscriptionUpgradeServlet extends HttpServlet {

    private final SubscriptionService subscriptionService = new SubscriptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (u != null) {
            User fresh = subscriptionService.refreshAndSync(u.getId());
            req.getSession().setAttribute(AppConstants.SESSION_USER, fresh);
            req.setAttribute("paymentHistory", subscriptionService.getPaymentsByUser(fresh.getId()));
            req.setAttribute("tierHistory", subscriptionService.getTierHistoryByUser(fresh.getId()));
        }

        req.setAttribute("planPrices", subscriptionService.getPlanPrices());
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
        String paymentMethod = req.getParameter("paymentMethod");

        try {
            if (plan != null && !plan.isBlank()) {
                days = Integer.parseInt(plan);
            }

            SubscriptionPayment payment = subscriptionService.fakePurchase(u.getId(), days, paymentMethod);
            User updated = subscriptionService.refreshAndSync(u.getId());

            req.getSession().setAttribute(AppConstants.SESSION_USER, updated);
            req.setAttribute("payment", payment);
            req.setAttribute("updatedUser", updated);

            req.getRequestDispatcher("/WEB-INF/jsp/common/subscription_result.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            req.setAttribute("planPrices", subscriptionService.getPlanPrices());
            req.setAttribute("paymentHistory", subscriptionService.getPaymentsByUser(u.getId()));
            req.setAttribute("tierHistory", subscriptionService.getTierHistoryByUser(u.getId()));
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/common/upgrade.jsp").forward(req, resp);
        }
    }
}
package com.freshmart.web.servlet.admin;

import com.freshmart.entity.User;
import com.freshmart.service.AppSettingService;
import com.freshmart.service.SubscriptionService;
import com.freshmart.service.dto.SubscriptionStatusDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = { "/admin/subscriptions" })
public class AdminSubscriptionServlet extends HttpServlet {

    private final SubscriptionService subscriptionService = new SubscriptionService();
    private final AppSettingService appSettingService = new AppSettingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = subscriptionService.getCustomerUsers();
        req.setAttribute("users", users);
        req.setAttribute("payments", subscriptionService.getAllPayments());
        req.setAttribute("tierHistory", subscriptionService.getAllTierHistory());
        req.setAttribute("settings", appSettingService.getAllAsMap());
        req.setAttribute("planPrices", subscriptionService.getPlanPrices());

        // Compute subscription status for each user
        Map<Long, SubscriptionStatusDTO> statusMap = new LinkedHashMap<>();
        for (User u : users) {
            statusMap.put(u.getId(), subscriptionService.computeStatus(u));
        }
        req.setAttribute("statusMap", statusMap);

        HttpSession session = req.getSession();
        Object success = session.getAttribute("flashSuccess");
        Object error = session.getAttribute("flashError");

        if (success != null) {
            req.setAttribute("successMessage", success.toString());
            session.removeAttribute("flashSuccess");
        }
        if (error != null) {
            req.setAttribute("errorMessage", error.toString());
            session.removeAttribute("flashError");
        }

        req.getRequestDispatcher("/WEB-INF/jsp/admin/subscriptions.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession();

        try {
            if ("grantPro".equals(action)) {
                Long userId = Long.parseLong(req.getParameter("userId"));
                int days = Integer.parseInt(req.getParameter("days"));
                String note = req.getParameter("note");

                subscriptionService.adminGrant(userId, days, note);
                session.setAttribute("flashSuccess", "Cấp/gia hạn PRO thành công.");

            } else if ("revokePro".equals(action)) {
                Long userId = Long.parseLong(req.getParameter("userId"));
                String note = req.getParameter("note");

                subscriptionService.revokePro(userId, note);
                session.setAttribute("flashSuccess", "Đã thu hồi PRO thành công.");

            } else if ("saveSettings".equals(action)) {
                Map<String, String> values = new LinkedHashMap<>();
                values.put(AppSettingService.LOW_STOCK_THRESHOLD, req.getParameter("lowStockThreshold"));
                values.put(AppSettingService.UPCOMING_EXPIRY_DAYS, req.getParameter("upcomingExpiryDays"));
                values.put(AppSettingService.REPLENISH_HISTORY_DAYS, req.getParameter("replenishHistoryDays"));
                values.put(AppSettingService.REPLENISH_LEAD_DAYS, req.getParameter("replenishLeadDays"));
                values.put(AppSettingService.REPLENISH_BUFFER_DAYS, req.getParameter("replenishBufferDays"));
                values.put(AppSettingService.REPLENISH_SAFETY_DAYS, req.getParameter("replenishSafetyDays"));

                // Subscription-specific settings
                values.put(AppSettingService.SUB_NOTIFY_DAYS, req.getParameter("subNotifyDays"));
                values.put(AppSettingService.SUB_GRACE_PERIOD_DAYS, req.getParameter("subGracePeriodDays"));

                appSettingService.saveSettings(values);
                session.setAttribute("flashSuccess", "Lưu cấu hình thành công.");
            } else {
                session.setAttribute("flashError", "Action không hợp lệ.");
            }
        } catch (Exception ex) {
            session.setAttribute("flashError", ex.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/subscriptions");
    }
}
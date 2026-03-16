package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.UserNotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = { "/subscription/notifications" })
public class SubscriptionNotificationsServlet extends HttpServlet {

    private final UserNotificationService notificationService = new UserNotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("notifications", notificationService.getRecentSubscriptionNotifications(user.getId(), 50));
        req.setAttribute("subscriptionUnreadCount", notificationService.countUnreadSubscriptionNotifications(user.getId()));
        req.getRequestDispatcher("/WEB-INF/jsp/common/subscription_notifications.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        notificationService.markAllSubscriptionRead(user.getId());
        resp.sendRedirect(req.getContextPath() + "/subscription/notifications?read=1");
    }
}

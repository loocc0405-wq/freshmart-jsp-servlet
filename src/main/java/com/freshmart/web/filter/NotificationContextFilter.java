package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.UserNotificationService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class NotificationContextFilter implements Filter {

    private final UserNotificationService userNotificationService = new UserNotificationService();

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        Object raw = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute(AppConstants.SESSION_USER);

        if (raw instanceof User) {
            User user = (User) raw;
            if (user.getRole() == com.freshmart.enums.Role.CUSTOMER) {
                request.setAttribute("subscriptionUnreadCount",
                        userNotificationService.countUnreadSubscriptionNotifications(user.getId()));
                request.setAttribute("subscriptionHeaderNotifications",
                        userNotificationService.getRecentSubscriptionNotifications(user.getId(), 5));
            }
        }

        chain.doFilter(req, resp);
    }
}

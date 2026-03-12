package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Order;
import com.freshmart.entity.User;
import com.freshmart.service.CustomerDashboardService;
import com.freshmart.service.CustomerOrderService;
import com.freshmart.service.dto.CustomerDashboardSummary;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/customer/dashboard"})
public class CustomerDashboardServlet extends HttpServlet {

    private final CustomerDashboardService customerDashboardService = new CustomerDashboardService();
    private final CustomerOrderService customerOrderService = new CustomerOrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User sessionUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            CustomerDashboardSummary summary = customerDashboardService.getSummary(sessionUser.getId());
            List<Order> orders = customerOrderService.getOrdersByCustomer(sessionUser.getId());
            List<Order> recentOrders = orders.size() > 5 ? orders.subList(0, 5) : orders;

            req.setAttribute("summary", summary);
            req.setAttribute("recentOrders", recentOrders);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/dashboard.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/customer/dashboard.jsp").forward(req, resp);
        }
    }
}
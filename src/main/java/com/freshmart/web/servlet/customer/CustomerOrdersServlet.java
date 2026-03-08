package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Order;
import com.freshmart.entity.User;
import com.freshmart.service.CustomerOrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/customer/orders"})
public class CustomerOrdersServlet extends HttpServlet {

    private static final int PAGE_SIZE = 5;

    private final CustomerOrderService customerOrderService = new CustomerOrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User sessionUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String status = req.getParameter("status");
        String fromDate = req.getParameter("fromDate");
        String toDate = req.getParameter("toDate");

        int page = 0;
        String pageParam = req.getParameter("page");
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                page = Math.max(0, Integer.parseInt(pageParam));
            } catch (NumberFormatException ignored) {
                page = 0;
            }
        }

        try {
            List<Order> orders = customerOrderService.getOrdersByFilters(
                    sessionUser.getId(),
                    status,
                    fromDate,
                    toDate,
                    page,
                    PAGE_SIZE
            );

            long totalItems = customerOrderService.countOrdersByFilters(
                    sessionUser.getId(),
                    status,
                    fromDate,
                    toDate
            );

            long totalPages = (long) Math.ceil((double) totalItems / PAGE_SIZE);

            req.setAttribute("orders", orders);
            req.setAttribute("selectedStatus", status);
            req.setAttribute("fromDate", fromDate);
            req.setAttribute("toDate", toDate);
            req.setAttribute("currentPage", page);
            req.setAttribute("pageSize", PAGE_SIZE);
            req.setAttribute("totalItems", totalItems);
            req.setAttribute("totalPages", totalPages);

            req.getRequestDispatcher("/WEB-INF/jsp/customer/orders.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/customer/orders.jsp").forward(req, resp);
        }
    }
}
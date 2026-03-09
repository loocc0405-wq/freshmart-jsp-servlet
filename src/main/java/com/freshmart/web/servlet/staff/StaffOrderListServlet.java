package com.freshmart.web.servlet.staff;

import com.freshmart.enums.OrderStatus;
import com.freshmart.service.StaffOrderOmsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/staff/orders"})
public class StaffOrderListServlet extends HttpServlet {

    private final StaffOrderOmsService omsService = new StaffOrderOmsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String rawStatus = req.getParameter("status");
            OrderStatus status = null;
            if (rawStatus != null && !rawStatus.isBlank()) {
                status = OrderStatus.valueOf(rawStatus.trim().toUpperCase());
            }

            req.setAttribute("selectedStatus", status != null ? status.name() : "");
            req.setAttribute("orders", omsService.listOrders(
                    status,
                    StaffOrderOmsService.DEFAULT_LIST_LIMIT,
                    StaffOrderOmsService.DEFAULT_NEAR_EXPIRY_DAYS
            ));
            req.setAttribute("nearExpiryWindowDays", StaffOrderOmsService.DEFAULT_NEAR_EXPIRY_DAYS);
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/order_list.jsp").forward(req, resp);
    }
}

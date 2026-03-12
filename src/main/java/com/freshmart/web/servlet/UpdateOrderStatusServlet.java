package com.freshmart.web.servlet;

import com.freshmart.enums.OrderStatus;
import com.freshmart.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/staff/order/update-status")
public class UpdateOrderStatusServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {

            Long orderId = Long.parseLong(req.getParameter("orderId"));
            String status = req.getParameter("status");

            OrderStatus newStatus = OrderStatus.valueOf(status);

            orderService.updateOrderStatus(orderId, newStatus);

        } catch (Exception ex) {

            req.getSession().setAttribute("errorMessage", ex.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/orders");
    }
}
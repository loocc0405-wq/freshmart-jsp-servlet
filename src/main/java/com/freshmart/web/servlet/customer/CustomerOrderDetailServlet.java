package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Order;
import com.freshmart.entity.User;
import com.freshmart.service.CustomerOrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/customer/order-detail"})
public class CustomerOrderDetailServlet extends HttpServlet {

    private final CustomerOrderService customerOrderService = new CustomerOrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User sessionUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String orderIdRaw = req.getParameter("id");
        if (orderIdRaw == null || orderIdRaw.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/customer/orders");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdRaw);
            Order order = customerOrderService.getOrderDetail(sessionUser.getId(), orderId);
            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/order_detail.jsp").forward(req, resp);
        } catch (NumberFormatException ex) {
            resp.sendRedirect(req.getContextPath() + "/customer/orders");
        } catch (RuntimeException ex) {
            req.setAttribute("errorMessage", ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/customer/order_detail.jsp").forward(req, resp);
        }
    }
}
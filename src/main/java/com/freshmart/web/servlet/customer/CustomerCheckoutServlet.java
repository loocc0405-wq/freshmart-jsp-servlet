package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/customer/checkout")
public class CustomerCheckoutServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute(AppConstants.SESSION_USER);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {

            var order = orderService.createCustomerOrder(user.getId());

            resp.sendRedirect(req.getContextPath()
                + "/customer/order-success?id=" + order.getId());

        }catch (Exception e) {

            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, resp);

        }
    }
}
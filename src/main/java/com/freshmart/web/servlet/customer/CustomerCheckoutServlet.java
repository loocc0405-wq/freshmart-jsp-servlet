package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.CartService;
import com.freshmart.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/customer/checkout")
public class CustomerCheckoutServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();

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
            resp.sendRedirect(req.getContextPath() + "/customer/order-success?id=" + order.getId());
        } catch (RuntimeException ex) {
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("items", cartService.getCartItems(user.getId()));
            req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, resp);
        }
    }
}
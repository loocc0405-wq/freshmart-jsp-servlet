package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.CartItem;
import com.freshmart.entity.User;
import com.freshmart.service.CartService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/checkout-review")
public class CheckoutReviewServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();

        User user = (User) session.getAttribute(AppConstants.SESSION_USER);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<CartItem> items = cartService.getCartItems(user.getId());

        if (items == null || items.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart-view");
            return;
        }

        req.setAttribute("items", items);

        req.getRequestDispatcher("/WEB-INF/jsp/order/checkout_review.jsp")
                .forward(req, resp);
    }
}
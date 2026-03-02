package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.CartService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ✅ LẤY ĐÚNG SESSION KEY
        User user = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");

        try {

            switch (action) {

                case "add": {
                    Long productId = Long.parseLong(req.getParameter("productId"));
                    int qty = Integer.parseInt(req.getParameter("qty"));
                    cartService.addToCart(user.getId(), productId, qty);
                    break;
                }

                case "update": {
                    Long productId = Long.parseLong(req.getParameter("productId"));
                    int newQty = Integer.parseInt(req.getParameter("qty"));
                    cartService.updateQuantity(user.getId(), productId, newQty);
                    break;
                }

                case "remove": {
                    Long productId = Long.parseLong(req.getParameter("productId"));
                    cartService.removeItem(user.getId(), productId);
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        resp.sendRedirect(req.getContextPath() + "/cart-view");
    }
}
package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.service.CartService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/cart-view")
public class CartViewServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession()
                .getAttribute(AppConstants.SESSION_USER);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("items",
                cartService.getCartItems(user.getId()));

        req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp")
                .forward(req, resp);
    }
}
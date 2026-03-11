package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.CartItem;
import com.freshmart.entity.User;
import com.freshmart.service.CartService;
import com.freshmart.util.GuestCartUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.util.List;

@WebServlet("/cart-view")
public class CartViewServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();

        User user = (User) session.getAttribute(AppConstants.SESSION_USER);

        List<CartItem> items;

        if (user == null) {

            items = GuestCartUtil.getGuestCart(session);

        } else {

            // ===== ADDED: MERGE GUEST CART INTO DB CART =====
            List<CartItem> guestCart = GuestCartUtil.getGuestCart(session);

            if (guestCart != null && !guestCart.isEmpty()) {

                for (CartItem item : guestCart) {

                    cartService.addToCart(
                            user.getId(),
                            item.getProduct().getId(),
                            item.getQuantity()
                    );
                }

                // clear guest cart after merge
                guestCart.clear();
                session.setAttribute(GuestCartUtil.SESSION_GUEST_CART, guestCart);
            }
            // ===== END ADDED =====

            items = cartService.getCartItems(user.getId());
        }

        req.setAttribute("items", items);

        req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp")
                .forward(req, resp);
    }
}
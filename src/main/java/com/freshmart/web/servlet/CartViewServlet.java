package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.CartItem;
import com.freshmart.entity.User;
import com.freshmart.service.CartService;
import com.freshmart.util.GuestCartUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/cart-view")
public class CartViewServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Object cartError = session.getAttribute("cartError");
        if (cartError != null) {
            req.setAttribute("error", cartError);
            session.removeAttribute("cartError");
        }

        User user = (User) session.getAttribute(AppConstants.SESSION_USER);

        List<CartItem> items;

        if (user == null) {
            items = GuestCartUtil.getGuestCart(session);
        } else {
            List<CartItem> guestCart = GuestCartUtil.getGuestCart(session);

            if (guestCart != null && !guestCart.isEmpty()) {
                try {
                    cartService.mergeCart(user.getId(), guestCart);
                    guestCart.clear();
                    session.setAttribute(GuestCartUtil.SESSION_GUEST_CART, guestCart);
                } catch (RuntimeException ex) {
                    req.setAttribute("error", ex.getMessage());
                }
            }

            items = cartService.getCartItems(user.getId());
        }

        req.setAttribute("items", items);
        req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, resp);
    }
}

package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Product;
import com.freshmart.entity.User;
import com.freshmart.repository.ProductRepository;
import com.freshmart.service.CartService;
import com.freshmart.util.GuestCartUtil;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();
    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute(AppConstants.SESSION_USER);
        String action = req.getParameter("action");

        try {
            if (user == null) {
                Long productId = Long.parseLong(req.getParameter("productId"));
                int qty = 1;

                if (req.getParameter("qty") != null) {
                    qty = Integer.parseInt(req.getParameter("qty"));
                }

                if (qty <= 0) {
                    qty = 1;
                }

                Product product = executor.execute(em -> productRepo.findById(em, productId).orElse(null));

                if (product != null) {
                    switch (action) {
                        case "add":
                            GuestCartUtil.addItem(session, product, qty);
                            break;
                        case "update":
                            GuestCartUtil.updateItem(session, productId, qty);
                            break;
                        case "remove":
                            GuestCartUtil.removeItem(session, productId);
                            break;
                        default:
                            break;
                    }
                }

                resp.sendRedirect(req.getContextPath() + "/cart-view");
                return;
            }

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
                default:
                    break;
            }
        } catch (Exception e) {
            session.setAttribute("cartError", e.getMessage() == null ? "Cart operation failed" : e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/cart-view");
    }
}

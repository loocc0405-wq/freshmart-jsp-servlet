package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.entity.Product;
import com.freshmart.service.CartService;
import com.freshmart.repository.ProductRepository;
import com.freshmart.util.GuestCartUtil;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    // ===== ADDED: for guest cart =====
    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    // ===== END ADDED =====

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();

        // ===== ADDED: CHECK USER =====
        User user = (User) session.getAttribute(AppConstants.SESSION_USER);
        // ===== END ADDED =====

        String action = req.getParameter("action");

        try {

            // =================================================
            // ===== ADDED: GUEST CART SUPPORT (BEFORE LOGIN)
            // =================================================
            if (user == null) {

                Long productId = Long.parseLong(req.getParameter("productId"));

                int qty = 1;

                if (req.getParameter("qty") != null) {
                    qty = Integer.parseInt(req.getParameter("qty"));
                }

                // ===== ADDED: VALIDATE QTY =====
                if (qty <= 0) {
                    qty = 1;
                }
                // ===== END ADDED =====

                Product product = executor.execute(
                        em -> productRepo.findById(em, productId).orElse(null)
                );

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
                    }
                }

                resp.sendRedirect(req.getContextPath() + "/cart-view");
                return;
            }
            // =================================================
            // ===== END ADDED
            // =================================================

            // ===== ORIGINAL CODE (GIỮ NGUYÊN) =====

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
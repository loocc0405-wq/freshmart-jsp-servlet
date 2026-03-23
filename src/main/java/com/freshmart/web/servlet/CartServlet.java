package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.CartItem;
import com.freshmart.entity.Product;
import com.freshmart.entity.User;
import com.freshmart.repository.ProductRepository;
import com.freshmart.service.CartService;
import com.freshmart.service.InventoryService;
import com.freshmart.util.GuestCartUtil;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();
    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final InventoryService inventoryService = new InventoryService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute(AppConstants.SESSION_USER);
        String action = req.getParameter("action");

        String productIdParam = req.getParameter("productId");

        try {
            if (user == null) {
                handleGuestCart(req, session, action);
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

            session.setAttribute("cartError",
                    e.getMessage() == null ? "Cart operation failed" : e.getMessage());

            if (productIdParam != null) {
                session.setAttribute("errorProductId", productIdParam);
            }
        }

        resp.sendRedirect(req.getContextPath() + "/cart-view");
    }

    private void handleGuestCart(HttpServletRequest req, HttpSession session, String action) {
        Long productId = Long.parseLong(req.getParameter("productId"));

        if ("remove".equals(action)) {
            GuestCartUtil.removeItem(session, productId);
            return;
        }

        int qty = 1;
        if (req.getParameter("qty") != null && !req.getParameter("qty").isBlank()) {
            qty = Integer.parseInt(req.getParameter("qty"));
        }

        if ("update".equals(action) && qty <= 0) {
            GuestCartUtil.removeItem(session, productId);
            return;
        }

        Product product = executor.execute(em -> productRepo.findById(em, productId).orElse(null));
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        if (!product.isActive()) {
            throw new IllegalStateException("Product is inactive");
        }

        List<CartItem> guestCart = GuestCartUtil.getGuestCart(session);
        int currentQty = currentGuestQty(guestCart, productId);
        int availableQty = executor.execute(em -> inventoryService.getAvailableQty(em, productId, LocalDate.now()));

        switch (action) {
            case "add": {
                if (qty <= 0) {
                    qty = 1;
                }
                if (availableQty <= 0) {
                    throw new IllegalStateException("Product out of stock");
                }

                if (currentQty + qty > availableQty) {
                    throw new IllegalStateException("Only " + availableQty + " items available in stock");
                }

                GuestCartUtil.addItem(session, product, qty);
                break;
            }
            case "update": {

                if (qty > availableQty) {
                    throw new IllegalStateException("Only " + availableQty + " items available in stock");
                }

                GuestCartUtil.updateItem(session, productId, qty);
                break;
            }
            default:
                break;
        }
    }

    private int currentGuestQty(List<CartItem> guestCart, Long productId) {
        if (guestCart == null || guestCart.isEmpty()) {
            return 0;
        }
        for (CartItem item : guestCart) {
            if (item != null
                    && item.getProduct() != null
                    && productId.equals(item.getProduct().getId())) {
                return item.getQuantity() == null ? 0 : item.getQuantity();
            }
        }
        return 0;
    }
}
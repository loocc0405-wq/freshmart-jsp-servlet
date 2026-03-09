package com.freshmart.web.servlet.seller;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Product;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.service.InventoryService;
import com.freshmart.util.JpaExecutor;
import com.freshmart.web.servlet.seller.dto.PosLine;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@WebServlet(urlPatterns = {"/seller/pos"})
public class SellerPosServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();
    private final InventoryService inventoryService = new InventoryService();

    private static final String SESSION_POS_SUCCESS = "sellerPosSuccessMessage";
    private static final String SESSION_POS_ERROR = "sellerPosErrorMessage";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) req.getSession().getAttribute(AppConstants.SESSION_SELLER_CART);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            req.getSession().setAttribute(AppConstants.SESSION_SELLER_CART, cart);
        }

        final Map<Long, Integer> cartFinal = cart;

        Map<Long, Integer> availableMap = new HashMap<>();
        Map<Long, LocalDate> nearestExpiryMap = new HashMap<>();
        Map<Long, Integer> cartShortageMap = new HashMap<>();
        boolean cartHasShortage = false;
        List<Product> products = new ArrayList<>();
        List<PosLine> lines = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        executor.execute(em -> {
            products.addAll(productRepo.findAll(em, false));

            for (Product p : products) {
                int available = inventoryService.getAvailableQty(em, p.getId(), today);
                availableMap.put(p.getId(), available);

                LocalDate nearest = lotRepo.findNearestExpiry(em, p.getId(), today);
                nearestExpiryMap.put(p.getId(), nearest);
            }

            // build cart view
            for (Map.Entry<Long, Integer> e : cartFinal.entrySet()) {
                Product p = em.find(Product.class, e.getKey());
                if (p == null) continue;
                PosLine line = new PosLine(p, e.getValue());
                lines.add(line);
            }
            return null;
        });

        for (PosLine l : lines) {
            total = total.add(l.getLineTotal());
            // Check shortage
            int available = availableMap.getOrDefault(l.getProduct().getId(), 0);
            int shortage = Math.max(0, l.getQuantity() - available);
            if (shortage > 0) {
                cartShortageMap.put(l.getProduct().getId(), shortage);
                cartHasShortage = true;
            }
        }

        req.setAttribute("products", products);
        req.setAttribute("availableMap", availableMap);
        req.setAttribute("nearestExpiryMap", nearestExpiryMap);
        req.setAttribute("today", today);
        req.setAttribute("cartShortageMap", cartShortageMap);
        req.setAttribute("cartHasShortage", cartHasShortage);

        req.setAttribute("lines", lines);
        req.setAttribute("total", total);

        req.getRequestDispatcher("/WEB-INF/jsp/seller/pos.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) req.getSession().getAttribute(AppConstants.SESSION_SELLER_CART);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            req.getSession().setAttribute(AppConstants.SESSION_SELLER_CART, cart);
        }

        try {
            String pidRaw = req.getParameter("productId");
            String qtyRaw = req.getParameter("quantity");

            if (pidRaw == null || pidRaw.isBlank() || qtyRaw == null || qtyRaw.isBlank()) {
                throw new IllegalArgumentException("Thiếu sản phẩm hoặc số lượng.");
            }

            Long pid = Long.parseLong(pidRaw.trim());
            int qty = Integer.parseInt(qtyRaw.trim());

            if (qty <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
            }

            final Map<Long, Integer> cartRef = cart;
            String successMessage = executor.execute(em -> {
                Product product = productRepo.findById(em, pid)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));

                if (!product.isActive()) {
                    throw new IllegalStateException("Sản phẩm đã ngừng kinh doanh.");
                }

                int available = inventoryService.getAvailableQty(em, pid, LocalDate.now());
                int currentQtyInCart = cartRef.getOrDefault(pid, 0);
                int newQty = currentQtyInCart + qty;

                if (available <= 0) {
                    throw new IllegalStateException("Sản phẩm hiện đã hết tồn khả dụng.");
                }

                if (newQty > available) {
                    throw new IllegalStateException(
                            "Không thể thêm vượt tồn khả dụng. Trong kho còn " + available + " đơn vị, giỏ hiện có "
                                    + currentQtyInCart + " đơn vị."
                    );
                }

                cartRef.put(pid, newQty);
                return "Đã thêm " + qty + " x " + product.getName() + " vào giỏ POS.";
            });

            req.getSession().setAttribute(SESSION_POS_SUCCESS, successMessage);
        } catch (RuntimeException ex) {
            req.getSession().setAttribute(SESSION_POS_ERROR, ex.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/seller/pos");
    }
}

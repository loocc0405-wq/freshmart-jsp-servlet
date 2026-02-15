package com.freshmart.web.servlet.seller;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Product;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.service.InventoryService;
import com.freshmart.util.JpaExecutor;
import com.freshmart.web.servlet.seller.dto.PosLine;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) req.getSession().getAttribute(AppConstants.SESSION_SELLER_CART);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            req.getSession().setAttribute(AppConstants.SESSION_SELLER_CART, cart);
        }

        Map<Long, Integer> availableMap = new HashMap<>();
        Map<Long, LocalDate> nearestExpiryMap = new HashMap<>();
        List<Product> products = new ArrayList<>();
        List<PosLine> lines = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        executor.execute(em -> {
            products.addAll(productRepo.findAll(em));

            for (Product p : products) {
                int available = inventoryService.getAvailableQty(em, p.getId(), today);
                availableMap.put(p.getId(), available);

                LocalDate nearest = lotRepo.findNearestExpiry(em, p.getId(), today);
                nearestExpiryMap.put(p.getId(), nearest);
            }

            // build cart view
            for (Map.Entry<Long, Integer> e : cart.entrySet()) {
                Product p = em.find(Product.class, e.getKey());
                if (p == null) continue;
                PosLine line = new PosLine(p, e.getValue());
                lines.add(line);
            }
            return null;
        });

        for (PosLine l : lines) {
            total = total.add(l.getLineTotal());
        }

        req.setAttribute("products", products);
        req.setAttribute("availableMap", availableMap);
        req.setAttribute("nearestExpiryMap", nearestExpiryMap);

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

        String pidRaw = req.getParameter("productId");
        String qtyRaw = req.getParameter("quantity");
        if (pidRaw != null && qtyRaw != null) {
            Long pid = Long.parseLong(pidRaw);
            int qty = Integer.parseInt(qtyRaw);
            if (qty > 0) {
                cart.put(pid, cart.getOrDefault(pid, 0) + qty);
            }
        }

        resp.sendRedirect(req.getContextPath() + "/seller/pos");
    }
}

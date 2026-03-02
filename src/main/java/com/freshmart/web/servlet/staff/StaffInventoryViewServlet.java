package com.freshmart.web.servlet.staff;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.InventoryService;
import com.freshmart.service.ProductLotService;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Servlet để xem tồn kho theo lô và FEFO.
 * Map: /staff/inventory
 */
@WebServlet(urlPatterns = {"/staff/inventory"})
public class StaffInventoryViewServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();
    private final InventoryService inventoryService = new InventoryService();
    private final ProductLotService lotService = new ProductLotService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String productIdRaw = req.getParameter("productId");

        List<Product> products = executor.execute(productRepo::findAll);
        req.setAttribute("products", products);

        if (productIdRaw != null && !productIdRaw.isBlank()) {
            try {
                Long productId = Long.parseLong(productIdRaw);

                Product product = executor.execute(em -> productRepo.findById(em, productId).orElse(null));
                if (product == null) {
                    throw new IllegalArgumentException("Product not found");
                }

                LocalDate today = LocalDate.now();

                // Available qty and lots
                int availableQty = inventoryService.getAvailableQty(executor.execute(em -> em), productId, today);
                List<ProductLot> availableLots = executor.execute(em -> lotRepo.findAvailableLotsFEFO(em, productId, today));

                // Expired lots
                List<ProductLot> expiredLots = lotService.getExpiredLotsForProduct(productId);

                // All lots (for history)
                List<ProductLot> allLots = lotService.getAllLotsForProduct(productId);

                // Summary
                Map<String, Integer> summary = lotService.getProductLotSummary(productId);

                // Upcoming expiry (within 7 days)
                List<ProductLot> upcomingExpiry = executor.execute(em -> em.createQuery(
                        "SELECT l FROM ProductLot l WHERE l.product.id = :pid AND l.qtyLeft > 0 " +
                                "AND l.expiryDate BETWEEN :today AND :deadline ORDER BY l.expiryDate ASC",
                        ProductLot.class
                ).setParameter("pid", productId)
                        .setParameter("today", today)
                        .setParameter("deadline", today.plusDays(7))
                        .getResultList());

                req.setAttribute("selectedProduct", product);
                req.setAttribute("availableQty", availableQty);
                req.setAttribute("availableLots", availableLots);
                req.setAttribute("expiredLots", expiredLots);
                req.setAttribute("allLots", allLots);
                req.setAttribute("upcomingExpiry", upcomingExpiry);
                req.setAttribute("summary", summary);

            } catch (Exception ex) {
                req.setAttribute("errorMessage", ex.getMessage());
            }
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff/inventory_view.jsp").forward(req, resp);
    }
}

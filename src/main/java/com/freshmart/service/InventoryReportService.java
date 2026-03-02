package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating inventory reports and analytics.
 */
public class InventoryReportService {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();

    /**
     * DTO for product inventory overview.
     */
    public static class ProductInventoryOverview {
        public Long productId;
        public String productName;
        public int totalQtyIn;
        public int totalQtyLeft;
        public int totalQtyConsumed;
        public int lotsCount;
        public int expiredLotsCount;
        public LocalDate nearestExpiry;
        public BigDecimal totalValue;

        public ProductInventoryOverview(Long productId, String productName, int totalQtyIn, 
                                       int totalQtyLeft, int lotsCount, int expiredLotsCount, 
                                       LocalDate nearestExpiry, BigDecimal totalValue) {
            this.productId = productId;
            this.productName = productName;
            this.totalQtyIn = totalQtyIn;
            this.totalQtyLeft = totalQtyLeft;
            this.totalQtyConsumed = totalQtyIn - totalQtyLeft;
            this.lotsCount = lotsCount;
            this.expiredLotsCount = expiredLotsCount;
            this.nearestExpiry = nearestExpiry;
            this.totalValue = totalValue;
        }
    }

    /**
     * Get inventory overview for all products.
     */
    public List<ProductInventoryOverview> getAllProductInventoryOverview() {
        return executor.execute(em -> {
            List<Product> products = productRepo.findAll(em);
            List<ProductInventoryOverview> result = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (Product p : products) {
                List<ProductLot> allLots = em.createQuery(
                        "SELECT l FROM ProductLot l WHERE l.product.id = :pid",
                        ProductLot.class
                ).setParameter("pid", p.getId()).getResultList();

                List<ProductLot> expiredLots = em.createQuery(
                        "SELECT l FROM ProductLot l WHERE l.product.id = :pid AND l.expiryDate < :today",
                        ProductLot.class
                ).setParameter("pid", p.getId()).setParameter("today", today).getResultList();

                int totalIn = allLots.stream().mapToInt(ProductLot::getQtyIn).sum();
                int totalLeft = allLots.stream().mapToInt(ProductLot::getQtyLeft).sum();

                LocalDate nearestExpiry = lotRepo.findNearestExpiry(em, p.getId(), today);

                BigDecimal totalValue = allLots.stream()
                        .map(l -> l.getImportPrice().multiply(BigDecimal.valueOf(l.getQtyLeft())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                result.add(new ProductInventoryOverview(
                        p.getId(), p.getName(), totalIn, totalLeft,
                        allLots.size(), expiredLots.size(), nearestExpiry, totalValue
                ));
            }

            return result;
        });
    }

    /**
     * Get products with low stock (below threshold).
     */
    public List<ProductInventoryOverview> getLowStockProducts(int threshold) {
        return getAllProductInventoryOverview().stream()
                .filter(o -> o.totalQtyLeft < threshold)
                .collect(Collectors.toList());
    }

    /**
     * Get products with upcoming expiry (within N days).
     */
    public List<ProductInventoryOverview> getProductsWithUpcomingExpiry(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return executor.execute(em -> {
            List<Product> upcomingProducts = em.createQuery(
                    "SELECT DISTINCT l.product FROM ProductLot l WHERE l.qtyLeft > 0 " +
                            "AND l.expiryDate BETWEEN :today AND :deadline",
                    Product.class
            ).setParameter("today", LocalDate.now())
                    .setParameter("deadline", deadline)
                    .getResultList();

            List<ProductInventoryOverview> result = new ArrayList<>();
            for (Product p : upcomingProducts) {
                List<ProductLot> allLots = em.createQuery(
                        "SELECT l FROM ProductLot l WHERE l.product.id = :pid",
                        ProductLot.class
                ).setParameter("pid", p.getId()).getResultList();

                int totalIn = allLots.stream().mapToInt(ProductLot::getQtyIn).sum();
                int totalLeft = allLots.stream().mapToInt(ProductLot::getQtyLeft).sum();

                LocalDate nearestExpiry = lotRepo.findNearestExpiry(em, p.getId(), LocalDate.now());

                BigDecimal totalValue = allLots.stream()
                        .map(l -> l.getImportPrice().multiply(BigDecimal.valueOf(l.getQtyLeft())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                result.add(new ProductInventoryOverview(
                        p.getId(), p.getName(), totalIn, totalLeft,
                        allLots.size(), 0, nearestExpiry, totalValue
                ));
            }

            return result;
        });
    }

    /**
     * Get total inventory value across all products.
     */
    public BigDecimal getTotalInventoryValue() {
        return executor.execute(em -> {
            Long value = em.createQuery(
                    "SELECT COALESCE(SUM(l.qtyLeft * l.importPrice), 0) FROM ProductLot l WHERE l.qtyLeft > 0",
                    Long.class
            ).getSingleResult();
            return BigDecimal.valueOf(value);
        });
    }

    /**
     * Count total lots waiting (not yet consumed).
     */
    public Long getTotalActiveLots() {
        return executor.execute(em -> em.createQuery(
                "SELECT COUNT(DISTINCT l.id) FROM ProductLot l WHERE l.qtyLeft > 0",
                Long.class
        ).getSingleResult());
    }

    /**
     * Get list of expired lots (for cleanup).
     */
    public List<ProductLot> getExpiredLotsForCleanup() {
        return executor.execute(em -> em.createQuery(
                "SELECT l FROM ProductLot l WHERE l.expiryDate < CURRENT_DATE ORDER BY l.expiryDate ASC",
                ProductLot.class
        ).getResultList());
    }
}

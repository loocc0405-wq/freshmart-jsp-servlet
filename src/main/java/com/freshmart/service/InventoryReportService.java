package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.InventoryLotFilter;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
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

        // ===== Add getters for JSP EL (JavaBean properties) =====
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getTotalQtyIn() { return totalQtyIn; }
        public int getTotalQtyLeft() { return totalQtyLeft; }
        public int getTotalQtyConsumed() { return totalQtyConsumed; }
        public int getLotsCount() { return lotsCount; }
        public int getExpiredLotsCount() { return expiredLotsCount; }
        public LocalDate getNearestExpiry() { return nearestExpiry; }
        public BigDecimal getTotalValue() { return totalValue; }
    }

    /**
     * DTO for inventory report snapshot with all metrics.
     */
    public static class InventoryReportSnapshot {
        private final List<ProductInventoryOverview> allProductsOverview;
        private final List<ProductInventoryOverview> lowStockProducts;
        private final List<ProductInventoryOverview> upcomingExpiryProducts;
        private final List<ProductLot> expiredLots;
        private final BigDecimal totalInventoryValue;
        private final Long totalActiveLots;
        private final int upcomingExpiryCount;
        private final int expiredLotsCount;

        public InventoryReportSnapshot(List<ProductInventoryOverview> allProductsOverview,
                                       List<ProductInventoryOverview> lowStockProducts,
                                       List<ProductInventoryOverview> upcomingExpiryProducts,
                                       List<ProductLot> expiredLots,
                                       BigDecimal totalInventoryValue,
                                       Long totalActiveLots,
                                       int upcomingExpiryCount,
                                       int expiredLotsCount) {
            this.allProductsOverview = allProductsOverview;
            this.lowStockProducts = lowStockProducts;
            this.upcomingExpiryProducts = upcomingExpiryProducts;
            this.expiredLots = expiredLots;
            this.totalInventoryValue = totalInventoryValue;
            this.totalActiveLots = totalActiveLots;
            this.upcomingExpiryCount = upcomingExpiryCount;
            this.expiredLotsCount = expiredLotsCount;
        }

        public List<ProductInventoryOverview> getAllProductsOverview() {
            return allProductsOverview;
        }

        public List<ProductInventoryOverview> getLowStockProducts() {
            return lowStockProducts;
        }

        public List<ProductInventoryOverview> getUpcomingExpiryProducts() {
            return upcomingExpiryProducts;
        }

        public List<ProductLot> getExpiredLots() {
            return expiredLots;
        }

        public BigDecimal getTotalInventoryValue() {
            return totalInventoryValue;
        }

        public Long getTotalActiveLots() {
            return totalActiveLots;
        }

        public int getUpcomingExpiryCount() {
            return upcomingExpiryCount;
        }

        public int getExpiredLotsCount() {
            return expiredLotsCount;
        }
    }

    /**
     * Get inventory overview for all products.
     */
    public List<ProductInventoryOverview> getAllProductInventoryOverview() {
        return executor.execute(em -> {
            List<Product> products = productRepo.findAll(em, false);
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
                        .map(l -> {
                            BigDecimal price = (l.getImportPrice() == null) ? BigDecimal.ZERO : l.getImportPrice();
                            return price.multiply(BigDecimal.valueOf(l.getQtyLeft()));
                        })
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
                        .map(l -> {
                            BigDecimal price = (l.getImportPrice() == null) ? BigDecimal.ZERO : l.getImportPrice();
                            return price.multiply(BigDecimal.valueOf(l.getQtyLeft()));
                        })
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
            BigDecimal value = em.createQuery(
                    "SELECT COALESCE(SUM(l.qtyLeft * COALESCE(l.importPrice, 0)), 0) " +
                    "FROM ProductLot l WHERE l.qtyLeft > 0",
                    BigDecimal.class
            ).getSingleResult();
            return value == null ? BigDecimal.ZERO : value;
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
                "SELECT l FROM ProductLot l JOIN FETCH l.product p " +
                "WHERE l.expiryDate < CURRENT_DATE ORDER BY l.expiryDate ASC",
                ProductLot.class
        ).getResultList());
    }

    /**
     * Check if filter has no active conditions.
     */
    private boolean isEmptyFilter(InventoryLotFilter filter) {
        return filter == null
                || (filter.getProductId() == null
                && filter.getSupplierId() == null
                && (filter.getStatus() == null || filter.getStatus().isBlank())
                && filter.getImportFrom() == null
                && filter.getImportTo() == null
                && filter.getExpiryFrom() == null
                && filter.getExpiryTo() == null
                && filter.getMinQtyLeft() == null
                && filter.getMaxQtyLeft() == null);
    }

    /**
     * Build overview for a product based on its lots.
     */
    private ProductInventoryOverview toOverview(Product product, List<ProductLot> lots, LocalDate today) {
        int totalIn = lots.stream().mapToInt(ProductLot::getQtyIn).sum();

        int totalRemainingAnyStatus = lots.stream()
                .mapToInt(ProductLot::getQtyLeft)
                .sum();

        int availableQty = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .mapToInt(ProductLot::getQtyLeft)
                .sum();

        int expiredLotsCount = (int) lots.stream()
                .filter(l -> l.getExpiryDate().isBefore(today))
                .count();

        LocalDate nearestExpiry = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .map(ProductLot::getExpiryDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        BigDecimal totalValue = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .map(l -> {
                    BigDecimal price = l.getImportPrice() == null ? BigDecimal.ZERO : l.getImportPrice();
                    return price.multiply(BigDecimal.valueOf(l.getQtyLeft()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ProductInventoryOverview overview = new ProductInventoryOverview(
                product.getId(),
                product.getName(),
                totalIn,
                availableQty,
                lots.size(),
                expiredLotsCount,
                nearestExpiry,
                totalValue
        );

        overview.totalQtyConsumed = totalIn - totalRemainingAnyStatus;
        return overview;
    }

    /**
     * Build report snapshot with all metrics based on filter conditions.
     */
    public InventoryReportSnapshot buildReportSnapshot(InventoryLotFilter filter,
                                                       int lowStockThreshold,
                                                       int upcomingExpiryDays) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            List<ProductLot> filteredLots = lotRepo.searchLots(em, filter, today);

            Map<Long, List<ProductLot>> lotsByProductId = filteredLots.stream()
                    .collect(Collectors.groupingBy(l -> l.getProduct().getId()));

            List<ProductInventoryOverview> allProductsOverview = new ArrayList<>();

            if (isEmptyFilter(filter)) {
                List<Product> allProducts = productRepo.findAll(em, false);

                for (Product p : allProducts) {
                    List<ProductLot> productLots = lotsByProductId.getOrDefault(p.getId(), Collections.emptyList());
                    allProductsOverview.add(toOverview(p, productLots, today));
                }
            } else {
                List<Product> filteredProducts = filteredLots.stream()
                        .map(ProductLot::getProduct)
                        .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a))
                        .values()
                        .stream()
                        .sorted(Comparator.comparing(Product::getId))
                        .collect(Collectors.toList());

                for (Product p : filteredProducts) {
                    List<ProductLot> productLots = lotsByProductId.getOrDefault(p.getId(), Collections.emptyList());
                    allProductsOverview.add(toOverview(p, productLots, today));
                }
            }

            List<ProductInventoryOverview> lowStockProducts = allProductsOverview.stream()
                    .filter(o -> o.getTotalQtyLeft() < lowStockThreshold)
                    .collect(Collectors.toList());

            LocalDate expiryDeadline = today.plusDays(upcomingExpiryDays);

            List<ProductInventoryOverview> upcomingExpiryProducts = allProductsOverview.stream()
                    .filter(o -> o.getNearestExpiry() != null)
                    .filter(o -> !o.getNearestExpiry().isBefore(today))
                    .filter(o -> !o.getNearestExpiry().isAfter(expiryDeadline))
                    .collect(Collectors.toList());

            List<ProductLot> expiredLots = filteredLots.stream()
                    .filter(l -> l.getExpiryDate().isBefore(today))
                    .sorted(Comparator.comparing(ProductLot::getExpiryDate)
                            .thenComparing(ProductLot::getId))
                    .collect(Collectors.toList());

            BigDecimal totalInventoryValue = filteredLots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .map(l -> {
                        BigDecimal price = l.getImportPrice() == null ? BigDecimal.ZERO : l.getImportPrice();
                        return price.multiply(BigDecimal.valueOf(l.getQtyLeft()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long totalActiveLots = filteredLots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .count();

            int upcomingExpiryCount = (int) filteredLots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .filter(l -> !l.getExpiryDate().isAfter(expiryDeadline))
                    .count();

            int expiredLotsCount = (int) expiredLots.stream().count();

            return new InventoryReportSnapshot(
                    allProductsOverview,
                    lowStockProducts,
                    upcomingExpiryProducts,
                    expiredLots,
                    totalInventoryValue,
                    totalActiveLots,
                    upcomingExpiryCount,
                    expiredLotsCount
            );
        });
    }
}

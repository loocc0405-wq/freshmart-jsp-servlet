package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.service.dto.InventoryLotFilter;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for generating inventory reports and analytics.
 */
public class InventoryReportService {

    public static final int DEFAULT_STAGNANT_DAYS = 30;

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
        public int availableQty;
        public int expiredQty;
        public int totalQtyConsumed;
        public int activeLotsCount;
        public int expiredLotsCount;
        public LocalDate nearestExpiry;
        public BigDecimal availableValue;

        public ProductInventoryOverview(Long productId,
                String productName,
                int totalQtyIn,
                int totalQtyLeft,
                int availableQty,
                int expiredQty,
                int activeLotsCount,
                int expiredLotsCount,
                LocalDate nearestExpiry,
                BigDecimal availableValue) {
            this.productId = productId;
            this.productName = productName;
            this.totalQtyIn = totalQtyIn;
            this.totalQtyLeft = totalQtyLeft;
            this.availableQty = availableQty;
            this.expiredQty = expiredQty;
            this.totalQtyConsumed = totalQtyIn - totalQtyLeft;
            this.activeLotsCount = activeLotsCount;
            this.expiredLotsCount = expiredLotsCount;
            this.nearestExpiry = nearestExpiry;
            this.availableValue = availableValue;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getTotalQtyIn() {
            return totalQtyIn;
        }

        public int getTotalQtyLeft() {
            return totalQtyLeft;
        }

        public int getAvailableQty() {
            return availableQty;
        }

        public int getExpiredQty() {
            return expiredQty;
        }

        public int getTotalQtyConsumed() {
            return totalQtyConsumed;
        }

        public int getActiveLotsCount() {
            return activeLotsCount;
        }

        public int getExpiredLotsCount() {
            return expiredLotsCount;
        }

        public LocalDate getNearestExpiry() {
            return nearestExpiry;
        }

        public BigDecimal getAvailableValue() {
            return availableValue;
        }
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
        private final BigDecimal nearExpiryValue;
        private final BigDecimal expiredValue;
        private final int stagnantLotsCount;
        private final BigDecimal stagnantValue;

        public InventoryReportSnapshot(List<ProductInventoryOverview> allProductsOverview,
                List<ProductInventoryOverview> lowStockProducts,
                List<ProductInventoryOverview> upcomingExpiryProducts,
                List<ProductLot> expiredLots,
                BigDecimal totalInventoryValue,
                Long totalActiveLots,
                int upcomingExpiryCount,
                int expiredLotsCount,
                BigDecimal nearExpiryValue,
                BigDecimal expiredValue,
                int stagnantLotsCount,
                BigDecimal stagnantValue) {
            this.allProductsOverview = allProductsOverview;
            this.lowStockProducts = lowStockProducts;
            this.upcomingExpiryProducts = upcomingExpiryProducts;
            this.expiredLots = expiredLots;
            this.totalInventoryValue = totalInventoryValue;
            this.totalActiveLots = totalActiveLots;
            this.upcomingExpiryCount = upcomingExpiryCount;
            this.expiredLotsCount = expiredLotsCount;
            this.nearExpiryValue = nearExpiryValue;
            this.expiredValue = expiredValue;
            this.stagnantLotsCount = stagnantLotsCount;
            this.stagnantValue = stagnantValue;
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

        public BigDecimal getNearExpiryValue() {
            return nearExpiryValue;
        }

        public BigDecimal getExpiredValue() {
            return expiredValue;
        }

        public int getStagnantLotsCount() {
            return stagnantLotsCount;
        }

        public BigDecimal getStagnantValue() {
            return stagnantValue;
        }
    }

    public List<ProductInventoryOverview> getAllProductInventoryOverview() {
        return executor.execute(em -> {
            List<Product> products = productRepo.findAll(em, true);
            Map<Long, List<ProductLot>> lotsByProductId = loadLotsByProductIds(
                    em,
                    products.stream().map(Product::getId).collect(Collectors.toSet()));

            List<ProductInventoryOverview> result = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (Product product : products) {
                List<ProductLot> productLots = lotsByProductId.getOrDefault(product.getId(), Collections.emptyList());
                if (!product.isActive() && productLots.isEmpty()) {
                    continue;
                }
                result.add(toOverview(product, productLots, today));
            }

            return result;
        });
    }

    public List<ProductInventoryOverview> getLowStockProducts(int threshold) {
        return getAllProductInventoryOverview().stream()
                .filter(o -> o.availableQty < threshold)
                .sorted(Comparator.comparingInt(ProductInventoryOverview::getAvailableQty)
                        .thenComparing(ProductInventoryOverview::getProductName))
                .collect(Collectors.toList());
    }

    public List<ProductInventoryOverview> getProductsWithUpcomingExpiry(int days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(Math.max(0, days));

        return getAllProductInventoryOverview().stream()
                .filter(o -> o.getNearestExpiry() != null)
                .filter(o -> !o.getNearestExpiry().isBefore(today))
                .filter(o -> !o.getNearestExpiry().isAfter(deadline))
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalInventoryValue() {
        return getAllProductInventoryOverview().stream()
                .map(ProductInventoryOverview::getAvailableValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getTotalActiveLots() {
        return getAllProductInventoryOverview().stream()
                .mapToLong(ProductInventoryOverview::getActiveLotsCount)
                .sum();
    }

    public List<ProductLot> getExpiredLotsForCleanup() {
        return executor.execute(em -> em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE l.expiryDate < CURRENT_DATE AND l.qtyLeft > 0 " +
                        "ORDER BY l.expiryDate ASC, l.id ASC",
                ProductLot.class).getResultList());
    }

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

    private BigDecimal lotRemainingValue(ProductLot lot) {
        if (lot == null || lot.getQtyLeft() == null || lot.getQtyLeft() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = lot.getImportPrice() == null ? BigDecimal.ZERO : lot.getImportPrice();
        return price.multiply(BigDecimal.valueOf(lot.getQtyLeft()));
    }

    private BigDecimal lotAvailableValue(ProductLot lot) {
        if (lot == null || lot.getAvailableToSell() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = lot.getImportPrice() == null ? BigDecimal.ZERO : lot.getImportPrice();
        return price.multiply(BigDecimal.valueOf(lot.getAvailableToSell()));
    }

    private ProductInventoryOverview toOverview(Product product, List<ProductLot> lots, LocalDate today) {
        int totalIn = lots.stream().mapToInt(ProductLot::getQtyIn).sum();

        int totalRemaining = lots.stream()
                .mapToInt(ProductLot::getQtyLeft)
                .sum();

        int availableQty = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .mapToInt(ProductLot::getAvailableToSell)
                .sum();

        int expiredQty = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> l.getExpiryDate().isBefore(today))
                .mapToInt(ProductLot::getQtyLeft)
                .sum();

        int activeLotsCount = (int) lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .count();

        int expiredLotsCount = (int) lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> l.getExpiryDate().isBefore(today))
                .count();

        LocalDate nearestExpiry = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .map(ProductLot::getExpiryDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        BigDecimal availableValue = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .map(this::lotAvailableValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductInventoryOverview(
                product.getId(),
                product.getName(),
                totalIn,
                totalRemaining,
                availableQty,
                expiredQty,
                activeLotsCount,
                expiredLotsCount,
                nearestExpiry,
                availableValue);
    }

    /**
     * Build report snapshot with all metrics based on filter conditions.
     *
     * Professional fix:
     * - lot filters are still respected to find the relevant product set,
     * - but product-level overview metrics are calculated from the FULL inventory
     * of those matched products instead of only the already-filtered subset.
     *
     * This prevents misleading metrics such as:
     * - availableQty becoming zero just because the current filter is EXPIRED,
     * - totalQtyIn appearing smaller when filtering by one supplier only,
     * - low stock / upcoming expiry lists being computed from partial lot slices.
     */
    public InventoryReportSnapshot buildReportSnapshot(InventoryLotFilter filter,
            int lowStockThreshold,
            int upcomingExpiryDays) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate expiryDeadline = today.plusDays(Math.max(0, upcomingExpiryDays));

            List<ProductLot> matchedLots = lotRepo.searchLots(em, filter, today);

            Set<Long> selectedProductIds;
            List<Product> selectedProducts;

            if (isEmptyFilter(filter)) {
                selectedProducts = productRepo.findAll(em, true);
                selectedProductIds = selectedProducts.stream()
                        .map(Product::getId)
                        .collect(Collectors.toSet());
            } else {
                selectedProductIds = matchedLots.stream()
                        .map(lot -> lot.getProduct().getId())
                        .collect(Collectors.toSet());

                if (filter.getProductId() != null) {
                    selectedProductIds.add(filter.getProductId());
                }

                if (selectedProductIds.isEmpty()) {
                    selectedProducts = List.of();
                } else {
                    Map<Long, Product> productsById = em.createQuery(
                            "SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id ASC",
                            Product.class).setParameter("ids", selectedProductIds)
                            .getResultList()
                            .stream()
                            .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a,
                                    LinkedHashMap::new));
                    selectedProducts = new ArrayList<>(productsById.values());
                }
            }

            Map<Long, List<ProductLot>> fullLotsByProductId = loadLotsByProductIds(em, selectedProductIds);

            List<ProductInventoryOverview> allProductsOverview = new ArrayList<>();
            for (Product product : selectedProducts) {
                List<ProductLot> productLots = fullLotsByProductId.getOrDefault(product.getId(),
                        Collections.emptyList());
                if (!product.isActive() && productLots.isEmpty()) {
                    continue;
                }
                allProductsOverview.add(toOverview(product, productLots, today));
            }

            List<ProductInventoryOverview> lowStockProducts = allProductsOverview.stream()
                    .filter(o -> o.getAvailableQty() < lowStockThreshold)
                    .sorted(Comparator.comparingInt(ProductInventoryOverview::getAvailableQty)
                            .thenComparing(ProductInventoryOverview::getProductName))
                    .collect(Collectors.toList());

            List<ProductInventoryOverview> upcomingExpiryProducts = allProductsOverview.stream()
                    .filter(o -> o.getNearestExpiry() != null)
                    .filter(o -> !o.getNearestExpiry().isBefore(today))
                    .filter(o -> !o.getNearestExpiry().isAfter(expiryDeadline))
                    .sorted(Comparator.comparing(ProductInventoryOverview::getNearestExpiry)
                            .thenComparing(ProductInventoryOverview::getProductName))
                    .collect(Collectors.toList());

            List<ProductLot> expiredLots = matchedLots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> l.getExpiryDate().isBefore(today))
                    .sorted(Comparator.comparing(ProductLot::getExpiryDate)
                            .thenComparing(ProductLot::getId))
                    .collect(Collectors.toList());

            BigDecimal totalInventoryValue = allProductsOverview.stream()
                    .map(ProductInventoryOverview::getAvailableValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long totalActiveLots = fullLotsByProductId.values().stream()
                    .flatMap(List::stream)
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .count();

            int upcomingExpiryCount = (int) fullLotsByProductId.values().stream()
                    .flatMap(List::stream)
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .filter(l -> !l.getExpiryDate().isAfter(expiryDeadline))
                    .count();

            int expiredLotsCount = expiredLots.size();

            BigDecimal nearExpiryValue = fullLotsByProductId.values().stream()
                    .flatMap(List::stream)
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .filter(l -> !l.getExpiryDate().isAfter(expiryDeadline))
                    .map(this::lotRemainingValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expiredValue = fullLotsByProductId.values().stream()
                    .flatMap(List::stream)
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> l.getExpiryDate().isBefore(today))
                    .map(this::lotRemainingValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            LocalDate stagnantThreshold = today.minusDays(DEFAULT_STAGNANT_DAYS);
            List<ProductLot> stagnantLots = fullLotsByProductId.values().stream()
                    .flatMap(List::stream)
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .filter(l -> l.getImportDate() != null && !l.getImportDate().isAfter(stagnantThreshold))
                    .collect(Collectors.toList());

            int stagnantLotsCount = stagnantLots.size();
            BigDecimal stagnantValue = stagnantLots.stream()
                    .map(this::lotRemainingValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new InventoryReportSnapshot(
                    allProductsOverview,
                    lowStockProducts,
                    upcomingExpiryProducts,
                    expiredLots,
                    totalInventoryValue,
                    totalActiveLots,
                    upcomingExpiryCount,
                    expiredLotsCount,
                    nearExpiryValue,
                    expiredValue,
                    stagnantLotsCount,
                    stagnantValue);
        });
    }

    private Map<Long, List<ProductLot>> loadLotsByProductIds(jakarta.persistence.EntityManager em,
            Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ProductLot> lots = em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE p.id IN :ids " +
                        "ORDER BY p.id ASC, l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class).setParameter("ids", productIds).getResultList();

        return lots.stream().collect(Collectors.groupingBy(
                lot -> lot.getProduct().getId(),
                LinkedHashMap::new,
                Collectors.toList()));
    }
}

package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.Supplier;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.StockSummaryDto;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing product lots (stock by batch).
 * Handles creation, viewing, and deletion of product lots.
 */
public class ProductLotService {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductLotRepository lotRepo = new ProductLotRepository();

    /**
     * Import a new lot (create/add stock).
     * MUST be called inside a transaction.
     */
    public ProductLot importLot(Long productId, Long supplierId,
                                LocalDate importDate, LocalDate expiryDate,
                                int quantity, BigDecimal importPrice,
                                jakarta.persistence.EntityManager em) {
        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Supplier supplier = null;
        if (supplierId != null) {
            supplier = em.find(Supplier.class, supplierId);
        }

        if (importDate == null || expiryDate == null) {
            throw new IllegalArgumentException("Import date and expiry date are required");
        }

        if (expiryDate.isBefore(importDate)) {
            throw new IllegalArgumentException("Expiry date must be after import date");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (importPrice != null && importPrice.signum() < 0) {
            throw new IllegalArgumentException("Import price must be >= 0");
        }

        ProductLot lot = new ProductLot();
        lot.setProduct(product);
        lot.setSupplier(supplier);
        lot.setImportDate(importDate);
        lot.setExpiryDate(expiryDate);
        lot.setQtyIn(quantity);
        lot.setQtyLeft(quantity);
        lot.setImportPrice(importPrice != null ? importPrice : BigDecimal.ZERO);

        em.persist(lot);
        return lot;
    }

    /**
     * Get all lots for a product (ordered by expiry date FEFO).
     */
    public List<ProductLot> getAllLotsForProduct(Long productId) {
        return executor.execute(em -> {
            return em.createQuery(
                    "SELECT l FROM ProductLot l " +
                    "JOIN FETCH l.product p " +
                    "LEFT JOIN FETCH l.supplier s " +
                    "WHERE p.id = :pid " +
                    "ORDER BY l.expiryDate ASC, l.importDate ASC",
                    ProductLot.class
            ).setParameter("pid", productId).getResultList();
        });
    }

    /**
     * Get available (non-expired) lots for a product.
     */
    public List<ProductLot> getAvailableLotsForProduct(Long productId) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            return lotRepo.findAvailableLotsFEFO(em, productId, today);
        });
    }

    /**
     * Get lot by ID.
     */
    public Optional<ProductLot> getLotById(Long lotId) {
        return executor.execute(em -> {
            ProductLot lot = em.find(ProductLot.class, lotId);
            return Optional.ofNullable(lot);
        });
    }

    /**
     * Get expired lots for a product (for cleanup/reporting).
     * Only returns lots where expiryDate < today AND qtyLeft > 0 (expired items needing cleanup).
     * Does not return already-consumed items (qtyLeft = 0).
     */
    public List<ProductLot> getExpiredLotsForProduct(Long productId) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            return em.createQuery(
                    "SELECT l FROM ProductLot l " +
                    "JOIN FETCH l.product p " +
                    "LEFT JOIN FETCH l.supplier s " +
                    "WHERE p.id = :pid AND l.expiryDate < :today AND l.qtyLeft > 0 ORDER BY l.expiryDate ASC",
                    ProductLot.class
            ).setParameter("pid", productId).setParameter("today", today).getResultList();
        });
    }

    /**
     * Get all lots expiring within N days (for alerts).
     */
    public List<ProductLot> getLotsExpiringWithinDays(int days) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate deadline = today.plusDays(days);
            return em.createQuery(
                    "SELECT l FROM ProductLot l " +
                    "JOIN FETCH l.product p " +
                    "LEFT JOIN FETCH l.supplier s " +
                    "WHERE l.expiryDate BETWEEN :today AND :deadline AND l.qtyLeft > 0 ORDER BY l.expiryDate ASC",
                    ProductLot.class
            ).setParameter("today", today).setParameter("deadline", deadline).getResultList();
        });
    }

    /**
     * Delete a lot (e.g., after expiry or waste).
     * Only allows deletion of lots that are expired (expiryDate < today) OR fully consumed (qtyLeft <= 0).
     * This prevents accidental deletion of active inventory.
     */
    public void deleteLot(Long lotId) {
        executor.executeVoid(em -> {
            ProductLot lot = em.find(ProductLot.class, lotId);
            if (lot == null) {
                throw new IllegalArgumentException("Lot not found: " + lotId);
            }

            LocalDate today = LocalDate.now();
            boolean expired = lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(today);
            boolean fullyConsumed = lot.getQtyLeft() != null && lot.getQtyLeft() <= 0;

            if (!expired && !fullyConsumed) {
                throw new IllegalStateException(
                    "Chỉ được loại bỏ lô đã hết hạn hoặc đã dùng hết để tránh sai lệch tồn kho."
                );
            }

            em.remove(lot);
        });
    }

    /**
     * Get total stock value for a product (sum of qty_left * import_price).
     * Only includes non-expired lots (expiryDate >= today).
     */
    public BigDecimal getTotalStockValue(Long productId) {
        return executor.execute(em -> {
            BigDecimal value = em.createQuery(
                    "SELECT COALESCE(SUM(l.qtyLeft * COALESCE(l.importPrice, 0)), 0) " +
                    "FROM ProductLot l WHERE l.product.id = :pid AND l.expiryDate >= :today",
                    BigDecimal.class
            ).setParameter("pid", productId)
             .setParameter("today", LocalDate.now())
             .getSingleResult();
            return value == null ? BigDecimal.ZERO : value;
        });
    }

    /**
     * Get summary: total qty_in, total qty_left, total qty_consumed for a product.
     */
    public java.util.Map<String, Integer> getProductLotSummary(Long productId) {
        return executor.execute(em -> {
            List<ProductLot> lots = em.createQuery(
                    "SELECT l FROM ProductLot l WHERE l.product.id = :pid",
                    ProductLot.class
            ).setParameter("pid", productId).getResultList();

            int totalIn = lots.stream().mapToInt(ProductLot::getQtyIn).sum();
            int totalLeft = lots.stream().mapToInt(ProductLot::getQtyLeft).sum();
            int totalConsumed = totalIn - totalLeft;

            java.util.Map<String, Integer> summary = new java.util.LinkedHashMap<>();
            summary.put("totalIn", totalIn);
            summary.put("totalLeft", totalLeft);
            summary.put("totalConsumed", totalConsumed);
            return summary;
        });
    }

    /**
     * Update an existing lot with safety rules.
     * Can edit: product, supplier, importDate, expiryDate, qtyIn, importPrice.
     * Cannot edit: qtyLeft (recalculated from consumed quantity).
     * MUST be called inside a transaction.
     */
    public ProductLot updateLot(Long lotId,
                                Long productId,
                                Long supplierId,
                                LocalDate importDate,
                                LocalDate expiryDate,
                                int newQtyIn,
                                BigDecimal importPrice,
                                jakarta.persistence.EntityManager em) {
        ProductLot lot = em.find(ProductLot.class, lotId);
        if (lot == null) {
            throw new IllegalArgumentException("Lot not found: " + lotId);
        }

        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Supplier supplier = null;
        if (supplierId != null) {
            supplier = em.find(Supplier.class, supplierId);
        }

        if (importDate == null || expiryDate == null) {
            throw new IllegalArgumentException("Import date and expiry date are required");
        }
        if (expiryDate.isBefore(importDate)) {
            throw new IllegalArgumentException("Expiry date must be after import date");
        }
        if (newQtyIn <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (importPrice != null && importPrice.signum() < 0) {
            throw new IllegalArgumentException("Import price must be >= 0");
        }

        int consumed = lot.getQtyIn() - lot.getQtyLeft();
        if (newQtyIn < consumed) {
            throw new IllegalArgumentException(
                "New qtyIn cannot be smaller than already consumed quantity: " + consumed
            );
        }

        // Prevent changing product if lot already has consumption history
        if (consumed > 0 && !lot.getProduct().getId().equals(productId)) {
            throw new IllegalStateException(
                "Không thể thay đổi sản phẩm cho lô đã có lịch sử tiêu thụ."
            );
        }

        lot.setProduct(product);
        lot.setSupplier(supplier);
        lot.setImportDate(importDate);
        lot.setExpiryDate(expiryDate);
        lot.setQtyIn(newQtyIn);
        lot.setQtyLeft(newQtyIn - consumed);
        lot.setImportPrice(importPrice != null ? importPrice : BigDecimal.ZERO);

        return em.merge(lot);
    }

    /**
     * Get lot detail with full references (product, supplier).
     */
    public Optional<ProductLot> getLotDetail(Long lotId) {
        return executor.execute(em -> Optional.ofNullable(lotRepo.findByIdWithRefs(em, lotId)));
    }

    /**
     * Get comprehensive stock summary for a product with clear semantics:
     * - totalIn: sum of all qtyIn (imported)
     * - totalRemaining: sum of all qtyLeft (physical stock)
     * - availableQty: sum of qtyLeft where expiryDate >= today (usable inventory)
     * - expiredQty: sum of qtyLeft where expiryDate < today AND qtyLeft > 0 (expired but in stock)
     * - consumedQty: totalIn - totalRemaining (actual usage)
     * - activeLotsCount: count of lots where expiryDate >= today
     * - expiredLotsCount: count of lots where expiryDate < today AND qtyLeft > 0
     * - expiringQty: sum of qtyLeft where expiryDate is within 7 days
     * - availableValue: sum of (qtyLeft * importPrice) where expiryDate >= today
     * - nearestExpiry: earliest expiryDate of all lots
     */
    public StockSummaryDto getStockSummary(Long productId) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            // Fetch all lots for the product
            List<ProductLot> lots = em.createQuery(
                    "SELECT l FROM ProductLot l WHERE l.product.id = :pid",
                    ProductLot.class
            ).setParameter("pid", productId).getResultList();

            if (lots.isEmpty()) {
                return new StockSummaryDto(0, 0, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO, null);
            }

            // Calculations
            int totalIn = lots.stream().mapToInt(ProductLot::getQtyIn).sum();
            int totalRemaining = lots.stream().mapToInt(ProductLot::getQtyLeft).sum();
            int consumedQty = totalIn - totalRemaining;

            int availableQty = lots.stream()
                .filter(l -> l.getExpiryDate().isAfter(today) || l.getExpiryDate().isEqual(today))
                .mapToInt(ProductLot::getQtyLeft)
                .sum();

            int expiredQty = lots.stream()
                .filter(l -> l.getExpiryDate().isBefore(today) && l.getQtyLeft() > 0)
                .mapToInt(ProductLot::getQtyLeft)
                .sum();

            long activeLotsCount = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> l.getExpiryDate().isAfter(today) || l.getExpiryDate().isEqual(today))
                .count();

            long expiredLotsCount = lots.stream()
                .filter(l -> l.getExpiryDate().isBefore(today) && l.getQtyLeft() > 0)
                .count();

            int expiringQty = lots.stream()
                .filter(l -> (l.getExpiryDate().isAfter(today) || l.getExpiryDate().isEqual(today)) &&
                       (l.getExpiryDate().isBefore(sevenDaysLater) || l.getExpiryDate().isEqual(sevenDaysLater)))
                .mapToInt(ProductLot::getQtyLeft)
                .sum();

            BigDecimal availableValue = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> l.getExpiryDate().isAfter(today) || l.getExpiryDate().isEqual(today))
                .map(l -> BigDecimal.valueOf(l.getQtyLeft())
                    .multiply(l.getImportPrice() != null ? l.getImportPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            LocalDate nearestExpiry = lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> l.getExpiryDate().isAfter(today) || l.getExpiryDate().isEqual(today))
                .map(ProductLot::getExpiryDate)
                .min(LocalDate::compareTo)
                .orElse(null);

            return new StockSummaryDto(
                totalIn,
                totalRemaining,
                availableQty,
                expiredQty,
                consumedQty,
                (int) activeLotsCount,
                (int) expiredLotsCount,
                expiringQty,
                availableValue,
                nearestExpiry
            );
        });
    }
}

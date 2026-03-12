package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.Supplier;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.StockSummaryDto;
import com.freshmart.util.JpaExecutor;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing product lots (stock by batch).
 * Handles creation, viewing, update and deletion rules for lots.
 */
public class ProductLotService {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductLotRepository lotRepo = new ProductLotRepository();

    /**
     * Import a new lot (create/add stock).
     * MUST be called inside a transaction.
     */
    public ProductLot importLot(Long productId,
                                Long supplierId,
                                LocalDate importDate,
                                LocalDate expiryDate,
                                int quantity,
                                BigDecimal importPrice,
                                EntityManager em) {
        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Supplier supplier = resolveSupplier(em, supplierId);
        validateLotInput(importDate, expiryDate, quantity, importPrice);

        ProductLot lot = new ProductLot();
        lot.setProduct(product);
        lot.setSupplier(supplier);
        lot.setImportDate(importDate);
        lot.setExpiryDate(expiryDate);
        lot.setQtyIn(quantity);
        lot.setQtyLeft(quantity);
        lot.setImportPrice(normalizePrice(importPrice));

        em.persist(lot);
        return lot;
    }

    /**
     * Get all lots for a product (ordered by expiry date FEFO).
     */
    public List<ProductLot> getAllLotsForProduct(Long productId) {
        return executor.execute(em -> em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE p.id = :pid " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class
        ).setParameter("pid", productId).getResultList());
    }

    /**
     * Get available (non-expired) lots for a product.
     */
    public List<ProductLot> getAvailableLotsForProduct(Long productId) {
        return executor.execute(em -> lotRepo.findAvailableLotsFEFO(em, productId, LocalDate.now()));
    }

    /**
     * Get lot by ID.
     */
    public Optional<ProductLot> getLotById(Long lotId) {
        return executor.execute(em -> Optional.ofNullable(em.find(ProductLot.class, lotId)));
    }

    /**
     * Get expired lots for a product (for cleanup/reporting).
     * Only returns lots where expiryDate < today AND qtyLeft > 0 (expired items needing cleanup).
     */
    public List<ProductLot> getExpiredLotsForProduct(Long productId) {
        return executor.execute(em -> em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE p.id = :pid AND l.expiryDate < :today AND l.qtyLeft > 0 " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class
        ).setParameter("pid", productId)
                .setParameter("today", LocalDate.now())
                .getResultList());
    }

    /**
     * Get all lots expiring within N days (for alerts).
     */
    public List<ProductLot> getLotsExpiringWithinDays(int days) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate deadline = today.plusDays(Math.max(0, days));
            return em.createQuery(
                    "SELECT l FROM ProductLot l " +
                            "JOIN FETCH l.product p " +
                            "LEFT JOIN FETCH l.supplier s " +
                            "WHERE l.expiryDate BETWEEN :today AND :deadline AND l.qtyLeft > 0 " +
                            "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                    ProductLot.class
            ).setParameter("today", today)
                    .setParameter("deadline", deadline)
                    .getResultList();
        });
    }

    /**
     * Delete a lot (e.g., after expiry or waste).
     * Only allows deletion of lots that are expired (expiryDate < today) OR fully consumed (qtyLeft <= 0).
     * NOTE: for production-grade inventory, physical deletion should eventually be replaced by
     * a dedicated disposal/audit flow. This method keeps the existing behavior for compatibility.
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
     * Only includes non-expired lots with qtyLeft > 0.
     */
    public BigDecimal getTotalStockValue(Long productId) {
        return executor.execute(em -> {
            BigDecimal value = em.createQuery(
                    "SELECT COALESCE(SUM(l.qtyLeft * COALESCE(l.importPrice, 0)), 0) " +
                            "FROM ProductLot l " +
                            "WHERE l.product.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today",
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
    public Map<String, Integer> getProductLotSummary(Long productId) {
        return executor.execute(em -> {
            List<ProductLot> lots = em.createQuery(
                    "SELECT l FROM ProductLot l WHERE l.product.id = :pid",
                    ProductLot.class
            ).setParameter("pid", productId).getResultList();

            int totalIn = lots.stream().mapToInt(ProductLot::getQtyIn).sum();
            int totalLeft = lots.stream().mapToInt(ProductLot::getQtyLeft).sum();
            int totalConsumed = totalIn - totalLeft;

            Map<String, Integer> summary = new LinkedHashMap<>();
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
                                EntityManager em) {
        ProductLot lot = em.find(ProductLot.class, lotId);
        if (lot == null) {
            throw new IllegalArgumentException("Lot not found: " + lotId);
        }

        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Supplier supplier = resolveSupplier(em, supplierId);
        validateLotInput(importDate, expiryDate, newQtyIn, importPrice);

        int currentQtyIn = lot.getQtyIn() == null ? 0 : lot.getQtyIn();
        int currentQtyLeft = lot.getQtyLeft() == null ? 0 : lot.getQtyLeft();
        int consumed = currentQtyIn - currentQtyLeft;

        if (newQtyIn < consumed) {
            throw new IllegalArgumentException(
                    "New qtyIn cannot be smaller than already consumed quantity: " + consumed
            );
        }

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
        lot.setImportPrice(normalizePrice(importPrice));

        return em.merge(lot);
    }

    /**
     * Get lot detail with full references (product, supplier).
     */
    public Optional<ProductLot> getLotDetail(Long lotId) {
        return executor.execute(em -> Optional.ofNullable(lotRepo.findByIdWithRefs(em, lotId)));
    }

    /**
     * Get comprehensive stock summary for a product with clear semantics.
     */
    public StockSummaryDto getStockSummary(Long productId) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            List<ProductLot> lots = em.createQuery(
                    "SELECT l FROM ProductLot l WHERE l.product.id = :pid",
                    ProductLot.class
            ).setParameter("pid", productId).getResultList();

            if (lots.isEmpty()) {
                return new StockSummaryDto(0, 0, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO, null);
            }

            int totalIn = lots.stream().mapToInt(ProductLot::getQtyIn).sum();
            int totalRemaining = lots.stream().mapToInt(ProductLot::getQtyLeft).sum();
            int consumedQty = totalIn - totalRemaining;

            int availableQty = lots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .mapToInt(ProductLot::getQtyLeft)
                    .sum();

            int expiredQty = lots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> l.getExpiryDate().isBefore(today))
                    .mapToInt(ProductLot::getQtyLeft)
                    .sum();

            long activeLotsCount = lots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .count();

            long expiredLotsCount = lots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> l.getExpiryDate().isBefore(today))
                    .count();

            int expiringQty = lots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .filter(l -> !l.getExpiryDate().isAfter(sevenDaysLater))
                    .mapToInt(ProductLot::getQtyLeft)
                    .sum();

            BigDecimal availableValue = lots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
                    .map(l -> BigDecimal.valueOf(l.getQtyLeft())
                            .multiply(l.getImportPrice() != null ? l.getImportPrice() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            LocalDate nearestExpiry = lots.stream()
                    .filter(l -> l.getQtyLeft() > 0)
                    .filter(l -> !l.getExpiryDate().isBefore(today))
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

    private Supplier resolveSupplier(EntityManager em, Long supplierId) {
        if (supplierId == null) {
            return null;
        }

        Supplier supplier = em.find(Supplier.class, supplierId);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier not found: " + supplierId);
        }
        return supplier;
    }

    private void validateLotInput(LocalDate importDate,
                                  LocalDate expiryDate,
                                  int quantity,
                                  BigDecimal importPrice) {
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
    }

    private BigDecimal normalizePrice(BigDecimal importPrice) {
        return importPrice != null ? importPrice : BigDecimal.ZERO;
    }
}
package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.Supplier;
import com.freshmart.entity.User;
import com.freshmart.enums.InventoryTransactionType;
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
 * Handles creation, viewing, update and disposal rules for lots.
 */
public class ProductLotService {

    private final JpaExecutor executor;
    private final ProductLotRepository lotRepo;
    private final InventoryAuditService auditService;

    public ProductLotService() {
        this(new JpaExecutor(), new ProductLotRepository(), new InventoryAuditService());
    }

    ProductLotService(JpaExecutor executor,
            ProductLotRepository lotRepo,
            InventoryAuditService auditService) {
        this.executor = executor;
        this.lotRepo = lotRepo;
        this.auditService = auditService;
    }

    public ProductLot importLot(Long productId,
            Long supplierId,
            LocalDate importDate,
            LocalDate expiryDate,
            int quantity,
            BigDecimal importPrice,
            EntityManager em) {
        return importLot(productId, supplierId, importDate, expiryDate, quantity, importPrice, null, em);
    }

    public ProductLot importLot(Long productId,
            Long supplierId,
            LocalDate importDate,
            LocalDate expiryDate,
            int quantity,
            BigDecimal importPrice,
            Long performedByUserId,
            EntityManager em) {
        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Supplier supplier = resolveSupplier(em, supplierId);
        User actor = resolveUser(em, performedByUserId);
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
        em.flush();

        auditService.recordImport(
                em,
                lot,
                actor,
                "Imported lot #" + lot.getId() + " for product " + product.getName());
        return lot;
    }

    public List<ProductLot> getAllLotsForProduct(Long productId) {
        return executor.execute(em -> em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE p.id = :pid " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class).setParameter("pid", productId).getResultList());
    }

    public List<ProductLot> getAvailableLotsForProduct(Long productId) {
        return executor.execute(em -> lotRepo.findAvailableLotsFEFO(em, productId, LocalDate.now()));
    }

    public Optional<ProductLot> getLotById(Long lotId) {
        return executor.execute(em -> Optional.ofNullable(em.find(ProductLot.class, lotId)));
    }

    public List<ProductLot> getExpiredLotsForProduct(Long productId) {
        return executor.execute(em -> em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE p.id = :pid AND l.expiryDate < :today AND l.qtyLeft > 0 " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class).setParameter("pid", productId)
                .setParameter("today", LocalDate.now())
                .getResultList());
    }

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
                    ProductLot.class).setParameter("today", today)
                    .setParameter("deadline", deadline)
                    .getResultList();
        });
    }

    /**
     * Legacy hard-delete flow kept only for backwards compatibility.
     * New UI should use disposeLot(...) instead so the system keeps an audit trail.
     */
    public void deleteLot(Long lotId) {
        executor.executeVoid(em -> {
            ProductLot lot = lotRepo.findByIdForUpdate(em, lotId);
            if (lot == null) {
                throw new IllegalArgumentException("Lot not found: " + lotId);
            }

            LocalDate today = LocalDate.now();
            boolean expired = lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(today);
            boolean fullyConsumed = lot.getQtyLeft() != null && lot.getQtyLeft() <= 0;

            if (!expired && !fullyConsumed) {
                throw new IllegalStateException(
                        "Chỉ được loại bỏ lô đã hết hạn hoặc đã dùng hết để tránh sai lệch tồn kho.");
            }

            Long allocationCount = em.createQuery(
                    "SELECT COUNT(a) FROM OrderItemLotAllocation a WHERE a.productLot.id = :lotId",
                    Long.class).setParameter("lotId", lotId).getSingleResult();

            Long reservationCount = em.createQuery(
                    "SELECT COUNT(r) FROM OrderItemLotReservation r WHERE r.productLot.id = :lotId",
                    Long.class).setParameter("lotId", lotId).getSingleResult();

            Long disposalCount = em.createQuery(
                    "SELECT COUNT(d) FROM LotDisposal d WHERE d.productLot.id = :lotId",
                    Long.class).setParameter("lotId", lotId).getSingleResult();

            Long importTransactionCount = em.createQuery(
                    "SELECT COUNT(t) FROM InventoryTransaction t WHERE t.productLot.id = :lotId AND t.type = :importType",
                    Long.class)
                    .setParameter("lotId", lotId)
                    .setParameter("importType", InventoryTransactionType.IMPORT)
                    .getSingleResult();

            Long nonImportTransactionCount = em.createQuery(
                    "SELECT COUNT(t) FROM InventoryTransaction t WHERE t.productLot.id = :lotId AND t.type <> :importType",
                    Long.class)
                    .setParameter("lotId", lotId)
                    .setParameter("importType", InventoryTransactionType.IMPORT)
                    .getSingleResult();

            if ((allocationCount != null && allocationCount > 0)
                    || (reservationCount != null && reservationCount > 0)
                    || (disposalCount != null && disposalCount > 0)
                    || (nonImportTransactionCount != null && nonImportTransactionCount > 0)
                    || (importTransactionCount != null && importTransactionCount > 1)) {
                throw new IllegalStateException(
                        "Lô đã có lịch sử reserve/audit/xuất kho/tiêu hủy nên không thể xóa cứng. Hãy giữ lịch sử hoặc dùng phiếu tiêu hủy.");
            }

            em.createQuery(
                    "DELETE FROM InventoryTransaction t WHERE t.productLot.id = :lotId").setParameter("lotId", lotId)
                    .executeUpdate();

            em.remove(lot);
        });
    }

    public ProductLot disposeLot(Long lotId,
            int disposeQty,
            String reason,
            String note,
            Long performedByUserId) {
        return executor.execute(em -> {
            ProductLot lot = lotRepo.findByIdForUpdate(em, lotId);
            if (lot == null) {
                throw new IllegalArgumentException("Lot not found: " + lotId);
            }
            if (disposeQty <= 0) {
                throw new IllegalArgumentException("Số lượng tiêu hủy phải lớn hơn 0.");
            }
            if (lot.getQtyLeft() == null || lot.getQtyLeft() <= 0) {
                throw new IllegalStateException("Lô không còn tồn để tiêu hủy.");
            }
            int availableToDispose = lot.getAvailableToSell();
            if (availableToDispose <= 0) {
                throw new IllegalStateException("Lô hiện không còn phần tồn khả dụng để tiêu hủy vì đã được reserve.");
            }
            if (disposeQty > availableToDispose) {
                throw new IllegalArgumentException(
                        "Không thể tiêu hủy vượt phần tồn chưa reserve của lô. Khả dụng để tiêu hủy: "
                                + availableToDispose);
            }
            String normalizedReason = noteOrThrow(reason, "Lý do tiêu hủy là bắt buộc.");
            User actor = resolveUser(em, performedByUserId);

            lot.setQtyLeft(lot.getQtyLeft() - disposeQty);
            em.merge(lot);
            auditService.recordDisposal(em, lot, disposeQty, normalizedReason, trimToNull(note), actor);
            return lot;
        });
    }

    public BigDecimal getTotalStockValue(Long productId) {
        return executor.execute(em -> {
            BigDecimal value = em.createQuery(
                    "SELECT COALESCE(SUM(l.qtyLeft * COALESCE(l.importPrice, 0)), 0) " +
                            "FROM ProductLot l " +
                            "WHERE l.product.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today",
                    BigDecimal.class).setParameter("pid", productId)
                    .setParameter("today", LocalDate.now())
                    .getSingleResult();
            return value == null ? BigDecimal.ZERO : value;
        });
    }

    public Map<String, Integer> getProductLotSummary(Long productId) {
        return executor.execute(em -> {
            List<ProductLot> lots = em.createQuery(
                    "SELECT l FROM ProductLot l WHERE l.product.id = :pid",
                    ProductLot.class).setParameter("pid", productId).getResultList();

            int totalIn = lots.stream().mapToInt(ProductLot::getQtyIn).sum();
            int totalLeft = lots.stream().mapToInt(ProductLot::getQtyLeft).sum();
            int totalReserved = lots.stream().mapToInt(l -> safeInt(l.getQtyReserved())).sum();
            int totalConsumed = totalIn - totalLeft;

            Map<String, Integer> summary = new LinkedHashMap<>();
            summary.put("totalIn", totalIn);
            summary.put("totalLeft", totalLeft);
            summary.put("totalReserved", totalReserved);
            summary.put("totalAvailableToSell", Math.max(0, totalLeft - totalReserved));
            summary.put("totalConsumed", totalConsumed);
            return summary;
        });
    }

    public ProductLot updateLot(Long lotId,
            Long productId,
            Long supplierId,
            LocalDate importDate,
            LocalDate expiryDate,
            int newQtyIn,
            BigDecimal importPrice,
            EntityManager em) {
        return updateLot(lotId, productId, supplierId, importDate, expiryDate, newQtyIn, importPrice, null, em);
    }

    public ProductLot updateLot(Long lotId,
            Long productId,
            Long supplierId,
            LocalDate importDate,
            LocalDate expiryDate,
            int newQtyIn,
            BigDecimal importPrice,
            Long performedByUserId,
            EntityManager em) {
        ProductLot lot = lotRepo.findByIdForUpdate(em, lotId);
        if (lot == null) {
            throw new IllegalArgumentException("Lot not found: " + lotId);
        }

        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        Supplier supplier = resolveSupplier(em, supplierId);
        User actor = resolveUser(em, performedByUserId);
        validateLotInput(importDate, expiryDate, newQtyIn, importPrice);

        int currentQtyIn = safeInt(lot.getQtyIn());
        int currentQtyLeft = safeInt(lot.getQtyLeft());
        int currentQtyReserved = safeInt(lot.getQtyReserved());
        int consumedOrDisposed = currentQtyIn - currentQtyLeft;

        if (newQtyIn < consumedOrDisposed) {
            throw new IllegalArgumentException(
                    "New qtyIn cannot be smaller than already consumed/disposed quantity: " + consumedOrDisposed);
        }
        if ((newQtyIn - consumedOrDisposed) < currentQtyReserved) {
            throw new IllegalArgumentException(
                    "New qtyIn would make qtyLeft smaller than reserved quantity: " + currentQtyReserved);
        }
        if (currentQtyReserved > 0 && expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Không thể đổi hạn sử dụng về quá khứ khi lô đang được reserve.");
        }

        if (consumedOrDisposed > 0 && !lot.getProduct().getId().equals(productId)) {
            throw new IllegalStateException(
                    "Không thể thay đổi sản phẩm cho lô đã có lịch sử xuất kho.");
        }

        int oldQtyLeft = currentQtyLeft;
        lot.setProduct(product);
        lot.setSupplier(supplier);
        lot.setImportDate(importDate);
        lot.setExpiryDate(expiryDate);
        lot.setQtyIn(newQtyIn);
        lot.setQtyLeft(newQtyIn - consumedOrDisposed);
        lot.setImportPrice(normalizePrice(importPrice));
        ProductLot merged = em.merge(lot);

        int deltaQtyLeft = merged.getQtyLeft() - oldQtyLeft;
        if (deltaQtyLeft != 0) {
            auditService.recordAdjustment(
                    em,
                    merged,
                    deltaQtyLeft,
                    actor,
                    "Adjusted lot #" + merged.getId() + " qty_left from " + oldQtyLeft + " to " + merged.getQtyLeft());
        }

        return merged;
    }

    public Optional<ProductLot> getLotDetail(Long lotId) {
        return executor.execute(em -> Optional.ofNullable(lotRepo.findByIdWithRefs(em, lotId)));
    }

    public StockSummaryDto getStockSummary(Long productId) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            List<ProductLot> lots = em.createQuery(
                    "SELECT l FROM ProductLot l WHERE l.product.id = :pid",
                    ProductLot.class).setParameter("pid", productId).getResultList();

            if (lots.isEmpty()) {
                return new StockSummaryDto(0, 0, 0, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO, null);
            }

            int totalIn = lots.stream().mapToInt(ProductLot::getQtyIn).sum();
            int totalRemaining = lots.stream().mapToInt(ProductLot::getQtyLeft).sum();
            int totalReserved = lots.stream().mapToInt(l -> safeInt(l.getQtyReserved())).sum();
            int consumedQty = totalIn - totalRemaining;

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
                    .map(l -> BigDecimal.valueOf(l.getAvailableToSell())
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
                    totalReserved,
                    availableQty,
                    expiredQty,
                    consumedQty,
                    (int) activeLotsCount,
                    (int) expiredLotsCount,
                    expiringQty,
                    availableValue,
                    nearestExpiry);
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

    private User resolveUser(EntityManager em, Long userId) {
        if (userId == null) {
            return null;
        }
        User user = em.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
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

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String noteOrThrow(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.ProductHealthRow;
import com.freshmart.service.dto.SupplierCandidate;
import com.freshmart.service.util.SupplierRankingUtil;
import com.freshmart.util.JpaExecutor;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service tính toán Product Health data cho màn quản lý sản phẩm.
 * Chỉ tính cho danh sách products của trang hiện tại (không toàn bộ DB).
 */
public class ProductHealthService {

    private static final int NEAR_EXPIRY_DAYS = 3;

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductLotRepository lotRepo = new ProductLotRepository();

    /**
     * Build health map cho danh sách products của trang hiện tại.
     * Key = productId, Value = ProductHealthRow
     */
    public Map<Long, ProductHealthRow> buildHealthMap(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Map.of();
        }

        return executor.execute(em -> {
            Map<Long, ProductHealthRow> map = new HashMap<>();
            LocalDate today = LocalDate.now();

            for (Product p : products) {
                ProductHealthRow row = buildRowForProduct(em, p, today);
                map.put(p.getId(), row);
            }

            return map;
        });
    }

    private ProductHealthRow buildRowForProduct(EntityManager em, Product p, LocalDate today) {
        Long productId = p.getId();

        // 1. Stock: tổng qtyLeft còn hạn
        int stock = lotRepo.getAvailableQty(em, productId, today);

        // 2. Near expiry: trong vòng 3 ngày
        int expiringQty = lotRepo.getExpiringQty(em, productId, today, NEAR_EXPIRY_DAYS);
        int expiringLots = lotRepo.countExpiringLots(em, productId, today, NEAR_EXPIRY_DAYS);

        // 3. Avg import price từ lịch sử product_lots
        BigDecimal avgImportPrice = getAvgImportPrice(em, productId);

        // 4. Estimated margin = sellPrice - avgImportPrice
        BigDecimal estimatedMargin = null;
        if (avgImportPrice != null && p.getSellPrice() != null) {
            estimatedMargin = p.getSellPrice().subtract(avgImportPrice).setScale(2, RoundingMode.HALF_UP);
        }

        ProductHealthRow row = new ProductHealthRow(productId, stock, expiringQty, expiringLots,
                avgImportPrice, estimatedMargin);

        // 5. Best supplier recommendation
        applySupplierRecommendation(em, productId, row);

        return row;
    }

    /**
     * Lấy avg import price từ lịch sử product_lots.
     * Trả về null nếu chưa có dữ liệu.
     */
    private BigDecimal getAvgImportPrice(EntityManager em, Long productId) {
        List<Double> result = em.createQuery(
                "SELECT AVG(l.importPrice) FROM ProductLot l " +
                "WHERE l.product.id = :pid AND l.importPrice IS NOT NULL AND l.importPrice > 0",
                Double.class
        ).setParameter("pid", productId).getResultList();

        if (result == null || result.isEmpty() || result.get(0) == null) {
            return null;
        }
        return BigDecimal.valueOf(result.get(0)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tái sử dụng logic supplier candidates từ ProductLotRepository
     * và ranking logic từ ReplenishmentService (extracted ra public method).
     */
    private void applySupplierRecommendation(EntityManager em, Long productId, ProductHealthRow row) {
        List<SupplierCandidate> candidates = lotRepo.findSupplierCandidates(em, productId);

        if (candidates.isEmpty()) {
            row.setRecommendationReason("Chưa có lịch sử supplier");
            return;
        }

        SupplierCandidate best = SupplierRankingUtil.pickBest(candidates);
        if (best == null) return;

        row.setRecommendedSupplierId(best.getSupplierId());
        row.setRecommendedSupplierName(best.getSupplierName());
        row.setRecommendedSupplierLeadTimeDays(best.getSupplierLeadTimeDays());
        row.setRecommendedSupplierAvgImportPrice(best.getAvgImportPrice());

        StringBuilder reason = new StringBuilder(best.getSupplierName());
        if (best.getSupplierLeadTimeDays() != null) {
            reason.append(" | LT: ").append(best.getSupplierLeadTimeDays()).append("d");
        }
        if (best.getAvgImportPrice() != null) {
            reason.append(" | Avg: ").append(best.getAvgImportPrice().setScale(0, RoundingMode.HALF_UP));
        }
        row.setRecommendationReason(reason.toString());
    }
}

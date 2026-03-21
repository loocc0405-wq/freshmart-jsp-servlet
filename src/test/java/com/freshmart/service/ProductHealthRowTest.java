package com.freshmart.service;

import com.freshmart.service.dto.ProductHealthRow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho ProductHealthRow badge priority logic.
 * Không cần DB - chỉ test pure logic trong DTO.
 */
class ProductHealthRowTest {

    // --- getHealthBadgeLabel ---

    @Test
    void badge_stockZero_returnsOut() {
        ProductHealthRow row = rowWith(0, 0);
        assertEquals("Out", row.getHealthBadgeLabel());
        assertEquals("bg-danger", row.getHealthBadgeCssClass());
    }

    @Test
    void badge_stockPositiveWithExpiring_returnsExpiryRisk() {
        ProductHealthRow row = rowWith(8, 8); // stock=8, expiringQty=8
        assertEquals("Expiry Risk", row.getHealthBadgeLabel());
        assertEquals("bg-warning text-dark", row.getHealthBadgeCssClass());
    }

    @Test
    void badge_expiryRiskTakesPriorityOverLow() {
        // stock=5 (<=10) AND expiringQty>0 → Expiry Risk, not Low
        ProductHealthRow row = rowWith(5, 3);
        assertEquals("Expiry Risk", row.getHealthBadgeLabel());
    }

    @Test
    void badge_stockLowNoExpiry_returnsLow() {
        ProductHealthRow row = rowWith(10, 0);
        assertEquals("Low", row.getHealthBadgeLabel());
        assertEquals("bg-info text-dark", row.getHealthBadgeCssClass());
    }

    @Test
    void badge_stockBoundary_exactly10_isLow() {
        ProductHealthRow row = rowWith(10, 0);
        assertEquals("Low", row.getHealthBadgeLabel());
    }

    @Test
    void badge_stockAbove10NoExpiry_returnsHealthy() {
        ProductHealthRow row = rowWith(11, 0);
        assertEquals("Healthy", row.getHealthBadgeLabel());
        assertEquals("bg-success", row.getHealthBadgeCssClass());
    }

    @Test
    void badge_largeStockNoExpiry_returnsHealthy() {
        ProductHealthRow row = rowWith(500, 0);
        assertEquals("Healthy", row.getHealthBadgeLabel());
    }

    // --- helper methods ---

    @Test
    void hasNearExpiry_whenExpiringQtyPositive_returnsTrue() {
        ProductHealthRow row = rowWith(20, 5);
        assertTrue(row.hasNearExpiry());
    }

    @Test
    void hasNearExpiry_whenZero_returnsFalse() {
        ProductHealthRow row = rowWith(20, 0);
        assertFalse(row.hasNearExpiry());
    }

    @Test
    void hasNegativeMargin_whenNull_returnsFalse() {
        ProductHealthRow row = new ProductHealthRow();
        assertFalse(row.hasNegativeMargin());
    }

    @Test
    void hasNegativeMargin_whenNegative_returnsTrue() {
        ProductHealthRow row = new ProductHealthRow();
        row.setEstimatedMargin(new BigDecimal("-100"));
        assertTrue(row.hasNegativeMargin());
    }

    @Test
    void hasNegativeMargin_whenZero_returnsFalse() {
        ProductHealthRow row = new ProductHealthRow();
        row.setEstimatedMargin(BigDecimal.ZERO);
        assertFalse(row.hasNegativeMargin());
    }

    @Test
    void hasSupplierRecommendation_whenNull_returnsFalse() {
        ProductHealthRow row = new ProductHealthRow();
        assertFalse(row.hasSupplierRecommendation());
    }

    @Test
    void hasSupplierRecommendation_whenSet_returnsTrue() {
        ProductHealthRow row = new ProductHealthRow();
        row.setRecommendedSupplierId(1L);
        assertTrue(row.hasSupplierRecommendation());
    }

    // --- factory helper ---

    private ProductHealthRow rowWith(int stock, int expiringQty) {
        ProductHealthRow row = new ProductHealthRow();
        row.setStock(stock);
        row.setExpiringQty(expiringQty);
        return row;
    }
}

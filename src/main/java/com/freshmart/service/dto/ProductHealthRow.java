package com.freshmart.service.dto;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin Product Health cho UI màn quản lý sản phẩm.
 * Bao gồm: stock, near expiry, avg import price, margin, best supplier.
 */
public class ProductHealthRow {
    private Long productId;
    
    // Stock info
    private int stock;
    
    // Near expiry info (3 days)
    private int expiringQty;
    private int expiringLots;
    
    // Pricing info
    private BigDecimal avgImportPrice;
    private BigDecimal estimatedMargin;
    
    // Best supplier recommendation
    private Long recommendedSupplierId;
    private String recommendedSupplierName;
    private Integer recommendedSupplierLeadTimeDays;
    private BigDecimal recommendedSupplierAvgImportPrice;
    private String recommendationReason;

    public ProductHealthRow() {}

    public ProductHealthRow(Long productId, int stock, int expiringQty, int expiringLots,
                           BigDecimal avgImportPrice, BigDecimal estimatedMargin) {
        this.productId = productId;
        this.stock = stock;
        this.expiringQty = expiringQty;
        this.expiringLots = expiringLots;
        this.avgImportPrice = avgImportPrice;
        this.estimatedMargin = estimatedMargin;
    }

    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getExpiringQty() { return expiringQty; }
    public void setExpiringQty(int expiringQty) { this.expiringQty = expiringQty; }

    public int getExpiringLots() { return expiringLots; }
    public void setExpiringLots(int expiringLots) { this.expiringLots = expiringLots; }

    public BigDecimal getAvgImportPrice() { return avgImportPrice; }
    public void setAvgImportPrice(BigDecimal avgImportPrice) { this.avgImportPrice = avgImportPrice; }

    public BigDecimal getEstimatedMargin() { return estimatedMargin; }
    public void setEstimatedMargin(BigDecimal estimatedMargin) { this.estimatedMargin = estimatedMargin; }

    public Long getRecommendedSupplierId() { return recommendedSupplierId; }
    public void setRecommendedSupplierId(Long recommendedSupplierId) { this.recommendedSupplierId = recommendedSupplierId; }

    public String getRecommendedSupplierName() { return recommendedSupplierName; }
    public void setRecommendedSupplierName(String recommendedSupplierName) { this.recommendedSupplierName = recommendedSupplierName; }

    public Integer getRecommendedSupplierLeadTimeDays() { return recommendedSupplierLeadTimeDays; }
    public void setRecommendedSupplierLeadTimeDays(Integer recommendedSupplierLeadTimeDays) { 
        this.recommendedSupplierLeadTimeDays = recommendedSupplierLeadTimeDays; 
    }

    public BigDecimal getRecommendedSupplierAvgImportPrice() { return recommendedSupplierAvgImportPrice; }
    public void setRecommendedSupplierAvgImportPrice(BigDecimal recommendedSupplierAvgImportPrice) { 
        this.recommendedSupplierAvgImportPrice = recommendedSupplierAvgImportPrice; 
    }

    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }

    // Helper methods for UI
    public boolean hasNearExpiry() {
        return expiringQty > 0 || expiringLots > 0;
    }

    public boolean hasNegativeMargin() {
        return estimatedMargin != null && estimatedMargin.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean hasSupplierRecommendation() {
        return recommendedSupplierId != null;
    }

    /**
     * Badge tổng hợp theo thứ tự ưu tiên:
     * 1. Out       - stock == 0
     * 2. Expiry Risk - stock > 0 và nearExpiryQty > 0
     * 3. Low       - stock > 0, nearExpiryQty == 0, stock <= 10
     * 4. Healthy   - còn lại
     */
    public String getHealthBadgeLabel() {
        if (stock == 0)          return "Out";
        if (expiringQty > 0)     return "Expiry Risk";
        if (stock <= 10)         return "Low";
        return "Healthy";
    }

    /** Bootstrap CSS class tương ứng với health badge label. */
    public String getHealthBadgeCssClass() {
        if (stock == 0)          return "bg-danger";
        if (expiringQty > 0)     return "bg-warning text-dark";
        if (stock <= 10)         return "bg-info text-dark";
        return "bg-success";
    }
}

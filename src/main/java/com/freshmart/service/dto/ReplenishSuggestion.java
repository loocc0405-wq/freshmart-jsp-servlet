package com.freshmart.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReplenishSuggestion {
    private Long productId;
    private String productName;

    private BigDecimal avg7;
    private BigDecimal avg30;
    private BigDecimal seasonFactor;
    private BigDecimal forecastPerDay;

    private int stock;
    private int suggestedQty;

    // NEW: for expiring lots adjustment + UI explain
    private int expiringQty;     // qty expiring within N days (e.g., 3)
    private int expiringLots;    // number of lots expiring within N days
    private String note;         // warning / explanation

    // NEW: for supplier recommendation
    private BigDecimal expectedDemand;
    private BigDecimal safetyStock;
    private BigDecimal reorderPoint;
    private Long recommendedSupplierId;
    private String recommendedSupplierName;
    private Integer recommendedSupplierLeadTimeDays;
    private BigDecimal recommendedSupplierAvgImportPrice;
    private LocalDate recommendedSupplierLastImportDate;
    private String recommendationReason;

    // Backward-compatible constructor (keeps old calls working)
    public ReplenishSuggestion(Long productId, String productName,
                              BigDecimal avg7, BigDecimal avg30,
                              BigDecimal seasonFactor, BigDecimal forecastPerDay,
                              int stock, int suggestedQty) {
        this(productId, productName, avg7, avg30, seasonFactor, forecastPerDay,
                stock, suggestedQty, 0, 0, null);
    }

    // NEW constructor (recommended)
    public ReplenishSuggestion(Long productId, String productName,
                              BigDecimal avg7, BigDecimal avg30,
                              BigDecimal seasonFactor, BigDecimal forecastPerDay,
                              int stock, int suggestedQty,
                              int expiringQty, int expiringLots,
                              String note) {
        this.productId = productId;
        this.productName = productName;
        this.avg7 = avg7;
        this.avg30 = avg30;
        this.seasonFactor = seasonFactor;
        this.forecastPerDay = forecastPerDay;
        this.stock = stock;
        this.suggestedQty = suggestedQty;
        this.expiringQty = expiringQty;
        this.expiringLots = expiringLots;
        this.note = note;
        // supplier fields default to null
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getAvg7() { return avg7; }
    public BigDecimal getAvg30() { return avg30; }
    public BigDecimal getSeasonFactor() { return seasonFactor; }
    public BigDecimal getForecastPerDay() { return forecastPerDay; }
    public int getStock() { return stock; }
    public int getSuggestedQty() { return suggestedQty; }

    // NEW getters
    public int getExpiringQty() { return expiringQty; }
    public int getExpiringLots() { return expiringLots; }
    public String getNote() { return note; }

    // Supplier recommendation getters
    public BigDecimal getExpectedDemand() { return expectedDemand; }
    public BigDecimal getSafetyStock() { return safetyStock; }
    public BigDecimal getReorderPoint() { return reorderPoint; }
    public Long getRecommendedSupplierId() { return recommendedSupplierId; }
    public String getRecommendedSupplierName() { return recommendedSupplierName; }
    public Integer getRecommendedSupplierLeadTimeDays() { return recommendedSupplierLeadTimeDays; }
    public BigDecimal getRecommendedSupplierAvgImportPrice() { return recommendedSupplierAvgImportPrice; }
    public LocalDate getRecommendedSupplierLastImportDate() { return recommendedSupplierLastImportDate; }
    public String getRecommendationReason() { return recommendationReason; }

    // Optional: setters if you want to modify later in service
    public void setSuggestedQty(int suggestedQty) { this.suggestedQty = suggestedQty; }
    public void setExpiringQty(int expiringQty) { this.expiringQty = expiringQty; }
    public void setExpiringLots(int expiringLots) { this.expiringLots = expiringLots; }
    public void setNote(String note) { this.note = note; }

    public void setExpectedDemand(BigDecimal expectedDemand) { this.expectedDemand = expectedDemand; }
    public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
    public void setReorderPoint(BigDecimal reorderPoint) { this.reorderPoint = reorderPoint; }
    public void setRecommendedSupplierId(Long recommendedSupplierId) { this.recommendedSupplierId = recommendedSupplierId; }
    public void setRecommendedSupplierName(String recommendedSupplierName) { this.recommendedSupplierName = recommendedSupplierName; }
    public void setRecommendedSupplierLeadTimeDays(Integer recommendedSupplierLeadTimeDays) { this.recommendedSupplierLeadTimeDays = recommendedSupplierLeadTimeDays; }
    public void setRecommendedSupplierAvgImportPrice(BigDecimal recommendedSupplierAvgImportPrice) { this.recommendedSupplierAvgImportPrice = recommendedSupplierAvgImportPrice; }
    public void setRecommendedSupplierLastImportDate(LocalDate recommendedSupplierLastImportDate) { this.recommendedSupplierLastImportDate = recommendedSupplierLastImportDate; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }
}
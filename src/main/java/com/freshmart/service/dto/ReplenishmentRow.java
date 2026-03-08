package com.freshmart.service.dto;

import java.math.BigDecimal;

public class ReplenishmentRow {

    private Long productId;
    private String productName;
    private String category;
    private String unit;

    private BigDecimal avgDailyDemand = BigDecimal.ZERO;
    private BigDecimal forecastDemand = BigDecimal.ZERO;
    private BigDecimal reorderPoint = BigDecimal.ZERO;
    private BigDecimal currentStock = BigDecimal.ZERO;
    private BigDecimal suggestedQty = BigDecimal.ZERO;

    private Integer expiringLots = 0;
    private BigDecimal expiringQty = BigDecimal.ZERO;

    public ReplenishmentRow() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getAvgDailyDemand() {
        return avgDailyDemand;
    }

    public void setAvgDailyDemand(BigDecimal avgDailyDemand) {
        this.avgDailyDemand = avgDailyDemand != null ? avgDailyDemand : BigDecimal.ZERO;
    }

    public BigDecimal getForecastDemand() {
        return forecastDemand;
    }

    public void setForecastDemand(BigDecimal forecastDemand) {
        this.forecastDemand = forecastDemand != null ? forecastDemand : BigDecimal.ZERO;
    }

    public BigDecimal getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(BigDecimal reorderPoint) {
        this.reorderPoint = reorderPoint != null ? reorderPoint : BigDecimal.ZERO;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock != null ? currentStock : BigDecimal.ZERO;
    }

    public BigDecimal getSuggestedQty() {
        return suggestedQty;
    }

    public void setSuggestedQty(BigDecimal suggestedQty) {
        this.suggestedQty = suggestedQty != null ? suggestedQty : BigDecimal.ZERO;
    }

    public Integer getExpiringLots() {
        return expiringLots;
    }

    public void setExpiringLots(Integer expiringLots) {
        this.expiringLots = expiringLots != null ? expiringLots : 0;
    }

    public BigDecimal getExpiringQty() {
        return expiringQty;
    }

    public void setExpiringQty(BigDecimal expiringQty) {
        this.expiringQty = expiringQty != null ? expiringQty : BigDecimal.ZERO;
    }
}
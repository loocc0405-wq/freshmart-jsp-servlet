package com.freshmart.service.dto;

import java.math.BigDecimal;

public class ReplenishSuggestion {
    private Long productId;
    private String productName;
    private BigDecimal avg7;
    private BigDecimal avg30;
    private BigDecimal seasonFactor;
    private BigDecimal forecastPerDay;
    private int stock;
    private int suggestedQty;

    public ReplenishSuggestion(Long productId, String productName,
                              BigDecimal avg7, BigDecimal avg30,
                              BigDecimal seasonFactor, BigDecimal forecastPerDay,
                              int stock, int suggestedQty) {
        this.productId = productId;
        this.productName = productName;
        this.avg7 = avg7;
        this.avg30 = avg30;
        this.seasonFactor = seasonFactor;
        this.forecastPerDay = forecastPerDay;
        this.stock = stock;
        this.suggestedQty = suggestedQty;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getAvg7() { return avg7; }
    public BigDecimal getAvg30() { return avg30; }
    public BigDecimal getSeasonFactor() { return seasonFactor; }
    public BigDecimal getForecastPerDay() { return forecastPerDay; }
    public int getStock() { return stock; }
    public int getSuggestedQty() { return suggestedQty; }
}
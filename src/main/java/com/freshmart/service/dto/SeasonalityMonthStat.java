package com.freshmart.service.dto;

import java.math.BigDecimal;

public class SeasonalityMonthStat {

    private int month;
    private String label;
    private BigDecimal avgDemand = BigDecimal.ZERO;
    private BigDecimal minDemand = BigDecimal.ZERO;
    private BigDecimal maxDemand = BigDecimal.ZERO;

    public SeasonalityMonthStat() {
    }

    public SeasonalityMonthStat(int month, String label, BigDecimal avgDemand, BigDecimal minDemand, BigDecimal maxDemand) {
        this.month = month;
        this.label = label;
        this.avgDemand = avgDemand != null ? avgDemand : BigDecimal.ZERO;
        this.minDemand = minDemand != null ? minDemand : BigDecimal.ZERO;
        this.maxDemand = maxDemand != null ? maxDemand : BigDecimal.ZERO;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getAvgDemand() {
        return avgDemand;
    }

    public void setAvgDemand(BigDecimal avgDemand) {
        this.avgDemand = avgDemand != null ? avgDemand : BigDecimal.ZERO;
    }

    public BigDecimal getMinDemand() {
        return minDemand;
    }

    public void setMinDemand(BigDecimal minDemand) {
        this.minDemand = minDemand != null ? minDemand : BigDecimal.ZERO;
    }

    public BigDecimal getMaxDemand() {
        return maxDemand;
    }

    public void setMaxDemand(BigDecimal maxDemand) {
        this.maxDemand = maxDemand != null ? maxDemand : BigDecimal.ZERO;
    }
}
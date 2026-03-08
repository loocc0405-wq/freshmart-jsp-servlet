package com.freshmart.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerDashboardSummary {

    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private BigDecimal totalSpent = BigDecimal.ZERO;

    private BigDecimal spentLast30Days = BigDecimal.ZERO;
    private BigDecimal averageCompletedOrderAmount = BigDecimal.ZERO;
    private BigDecimal latestCompletedOrderAmount = BigDecimal.ZERO;
    private LocalDateTime latestCompletedAt;

    private BigDecimal spendingAlertThreshold = BigDecimal.ZERO;
    private boolean overSpendingThreshold;

    public CustomerDashboardSummary() {
    }

    public CustomerDashboardSummary(long totalOrders,
                                    long pendingOrders,
                                    long completedOrders,
                                    BigDecimal totalSpent) {
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.completedOrders = completedOrders;
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
    }

    public BigDecimal getSpentLast30Days() {
        return spentLast30Days;
    }

    public void setSpentLast30Days(BigDecimal spentLast30Days) {
        this.spentLast30Days = spentLast30Days != null ? spentLast30Days : BigDecimal.ZERO;
    }

    public BigDecimal getAverageCompletedOrderAmount() {
        return averageCompletedOrderAmount;
    }

    public void setAverageCompletedOrderAmount(BigDecimal averageCompletedOrderAmount) {
        this.averageCompletedOrderAmount = averageCompletedOrderAmount != null ? averageCompletedOrderAmount : BigDecimal.ZERO;
    }

    public BigDecimal getLatestCompletedOrderAmount() {
        return latestCompletedOrderAmount;
    }

    public void setLatestCompletedOrderAmount(BigDecimal latestCompletedOrderAmount) {
        this.latestCompletedOrderAmount = latestCompletedOrderAmount != null ? latestCompletedOrderAmount : BigDecimal.ZERO;
    }

    public LocalDateTime getLatestCompletedAt() {
        return latestCompletedAt;
    }

    public void setLatestCompletedAt(LocalDateTime latestCompletedAt) {
        this.latestCompletedAt = latestCompletedAt;
    }

    public BigDecimal getSpendingAlertThreshold() {
        return spendingAlertThreshold;
    }

    public void setSpendingAlertThreshold(BigDecimal spendingAlertThreshold) {
        this.spendingAlertThreshold = spendingAlertThreshold != null ? spendingAlertThreshold : BigDecimal.ZERO;
    }

    public boolean isOverSpendingThreshold() {
        return overSpendingThreshold;
    }

    public void setOverSpendingThreshold(boolean overSpendingThreshold) {
        this.overSpendingThreshold = overSpendingThreshold;
    }
}
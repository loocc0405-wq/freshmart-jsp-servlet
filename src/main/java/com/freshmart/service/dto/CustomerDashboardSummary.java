package com.freshmart.service.dto;

import java.math.BigDecimal;

public class CustomerDashboardSummary {

    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private BigDecimal totalSpent = BigDecimal.ZERO;

    public CustomerDashboardSummary() {
    }

    public CustomerDashboardSummary(long totalOrders, long pendingOrders, long completedOrders, BigDecimal totalSpent) {
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
}
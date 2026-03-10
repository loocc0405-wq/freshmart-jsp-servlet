package com.freshmart.service.dto;

import java.time.LocalDate;

public class StaffOrderItemAssessment {
    private final Long productId;
    private final String productName;
    private final int requestedQty;
    private final FefoAllocationPlan plan;

    public StaffOrderItemAssessment(Long productId, String productName, int requestedQty, FefoAllocationPlan plan) {
        this.productId = productId;
        this.productName = productName;
        this.requestedQty = requestedQty;
        this.plan = plan;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getRequestedQty() { return requestedQty; }
    public FefoAllocationPlan getPlan() { return plan; }

    public int getAvailableQty() { return plan.getAvailableQty(); }
    public int getNearExpiryQty() { return plan.getNearExpiryQty(); }
    public int getShortageQty() { return plan.getShortageQty(); }
    public LocalDate getNearestExpiry() { return plan.getNearestExpiry(); }
    public boolean isEnoughStock() { return plan.isEnoughStock(); }
    public boolean isUsesNearExpiryLots() { return plan.isUsesNearExpiryLots(); }
}

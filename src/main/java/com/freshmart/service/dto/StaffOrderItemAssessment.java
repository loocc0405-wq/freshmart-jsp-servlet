package com.freshmart.service.dto;

import com.freshmart.entity.OrderItemLotAllocation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class StaffOrderItemAssessment {
    private final Long orderItemId;
    private final Long productId;
    private final String productName;
    private final int requestedQty;
    private final FefoAllocationPlan plan;
    private final List<OrderItemLotAllocation> actualAllocations;

    public StaffOrderItemAssessment(Long orderItemId,
                                    Long productId,
                                    String productName,
                                    int requestedQty,
                                    FefoAllocationPlan plan,
                                    List<OrderItemLotAllocation> actualAllocations) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.requestedQty = requestedQty;
        this.plan = plan;
        this.actualAllocations = actualAllocations == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(actualAllocations);
    }

    public Long getOrderItemId() { return orderItemId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getRequestedQty() { return requestedQty; }
    public FefoAllocationPlan getPlan() { return plan; }
    public List<OrderItemLotAllocation> getActualAllocations() { return actualAllocations; }

    public int getAvailableQty() { return plan.getAvailableQty(); }
    public int getNearExpiryQty() { return plan.getNearExpiryQty(); }
    public int getShortageQty() { return plan.getShortageQty(); }
    public LocalDate getNearestExpiry() { return plan.getNearestExpiry(); }
    public boolean isEnoughStock() { return plan.isEnoughStock(); }
    public boolean isUsesNearExpiryLots() { return plan.isUsesNearExpiryLots(); }
    public boolean isHasActualAllocations() { return !actualAllocations.isEmpty(); }
    public int getActualAllocatedQty() {
        return actualAllocations.stream().mapToInt(a -> a.getAllocatedQty() != null ? a.getAllocatedQty() : 0).sum();
    }
}

package com.freshmart.service.dto;

import com.freshmart.entity.Order;

import java.util.Collections;
import java.util.List;

public class StaffOrderDetailView {
    private final Order order;
    private final List<StaffOrderItemAssessment> itemAssessments;
    private final int nearExpiryWindowDays;
    private final boolean allFulfillable;
    private final int riskyItemCount;

    public StaffOrderDetailView(Order order,
                                List<StaffOrderItemAssessment> itemAssessments,
                                int nearExpiryWindowDays,
                                boolean allFulfillable,
                                int riskyItemCount) {
        this.order = order;
        this.itemAssessments = itemAssessments == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(itemAssessments);
        this.nearExpiryWindowDays = nearExpiryWindowDays;
        this.allFulfillable = allFulfillable;
        this.riskyItemCount = riskyItemCount;
    }

    public Order getOrder() { return order; }
    public List<StaffOrderItemAssessment> getItemAssessments() { return itemAssessments; }
    public int getNearExpiryWindowDays() { return nearExpiryWindowDays; }
    public boolean isAllFulfillable() { return allFulfillable; }
    public int getRiskyItemCount() { return riskyItemCount; }
}

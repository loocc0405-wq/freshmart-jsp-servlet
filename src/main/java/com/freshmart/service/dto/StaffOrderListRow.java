package com.freshmart.service.dto;

import com.freshmart.entity.Order;

public class StaffOrderListRow {
    private final Order order;
    private final int itemCount;
    private final boolean pendingFulfillable;
    private final boolean nearExpiryPriority;

    public StaffOrderListRow(Order order,
                             int itemCount,
                             boolean pendingFulfillable,
                             boolean nearExpiryPriority) {
        this.order = order;
        this.itemCount = itemCount;
        this.pendingFulfillable = pendingFulfillable;
        this.nearExpiryPriority = nearExpiryPriority;
    }

    public Order getOrder() { return order; }
    public int getItemCount() { return itemCount; }
    public boolean isPendingFulfillable() { return pendingFulfillable; }
    public boolean isNearExpiryPriority() { return nearExpiryPriority; }
}

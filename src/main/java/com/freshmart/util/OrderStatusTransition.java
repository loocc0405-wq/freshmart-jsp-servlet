package com.freshmart.util;

import com.freshmart.enums.OrderStatus;

import java.util.Map;
import java.util.Set;

public class OrderStatusTransition {

    private static final Map<OrderStatus, Set<OrderStatus>> MATRIX = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELED),
            OrderStatus.SHIPPING, Set.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELED, Set.of()
    );

    public static boolean isAllowed(OrderStatus from, OrderStatus to) {

        if (from == null || to == null) return false;

        Set<OrderStatus> allowed = MATRIX.get(from);

        return allowed != null && allowed.contains(to);
    }
}
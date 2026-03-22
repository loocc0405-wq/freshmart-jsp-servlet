package com.freshmart.service;

import com.freshmart.entity.Order;
import com.freshmart.entity.OrderItem;
import com.freshmart.entity.OrderItemLotAllocation;
import com.freshmart.entity.OrderItemLotReservation;
import com.freshmart.entity.ProductLot;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.OrderItemLotAllocationRepository;
import com.freshmart.repository.OrderItemLotReservationRepository;
import com.freshmart.repository.OrderRepository;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.FefoAllocationLot;
import com.freshmart.service.dto.FefoAllocationPlan;
import com.freshmart.service.dto.StaffOrderDetailView;
import com.freshmart.service.dto.StaffOrderItemAssessment;
import com.freshmart.service.dto.StaffOrderListRow;
import com.freshmart.util.FEFOUtil;
import com.freshmart.util.JpaExecutor;
import com.freshmart.util.OrderFefoPlanner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffOrderOmsService {

    public static final int DEFAULT_LIST_LIMIT = 50;
    public static final int DEFAULT_NEAR_EXPIRY_DAYS = 3;

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderRepository orderRepo = new OrderRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();
    private final OrderItemLotAllocationRepository allocationRepository = new OrderItemLotAllocationRepository();
    private final OrderItemLotReservationRepository reservationRepository = new OrderItemLotReservationRepository();

    public List<StaffOrderListRow> listOrders(OrderStatus status, int limit, int nearExpiryDays) {
        return executor.execute(em -> {
            List<Order> orders = orderRepo.findForStaffList(em, status, Math.max(1, limit));
            LocalDate today = LocalDate.now();
            List<StaffOrderListRow> rows = new ArrayList<>();

            for (Order order : orders) {
                boolean inventoryCheckRelevant = order.getStatus() == OrderStatus.PENDING
                        || order.getStatus() == OrderStatus.PROCESSING
                        || order.getStatus() == OrderStatus.SHIPPING;

                boolean fulfillable = true;
                boolean hasNearExpiry = false;

                if (inventoryCheckRelevant) {
                    for (OrderItem item : order.getItems()) {
                        FefoAllocationPlan plan = buildPlanForOrderItem(em, item, today, nearExpiryDays);
                        fulfillable &= plan.isEnoughStock();
                        hasNearExpiry |= plan.isUsesNearExpiryLots() || plan.getNearExpiryQty() > 0;
                    }
                }

                rows.add(new StaffOrderListRow(order, order.getItems().size(), fulfillable, hasNearExpiry));
            }

            return rows;
        });
    }

    public StaffOrderDetailView getOrderDetail(Long orderId, int nearExpiryDays) {
        return executor.execute(em -> {
            Order order = orderRepo.findByIdWithRefs(em, orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            Map<Long, List<OrderItemLotAllocation>> allocationsByOrderItemId = new HashMap<>();
            for (OrderItemLotAllocation allocation : allocationRepository.findByOrderId(em, orderId)) {
                allocationsByOrderItemId
                        .computeIfAbsent(allocation.getOrderItem().getId(), key -> new ArrayList<>())
                        .add(allocation);
            }

            LocalDate today = LocalDate.now();
            List<StaffOrderItemAssessment> assessments = new ArrayList<>();
            boolean allFulfillable = true;
            int riskyItemCount = 0;

            for (OrderItem item : order.getItems()) {
                FefoAllocationPlan plan = buildPlanForOrderItem(em, item, today, nearExpiryDays);

                StaffOrderItemAssessment assessment = new StaffOrderItemAssessment(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        plan,
                        allocationsByOrderItemId.getOrDefault(item.getId(), List.of()));

                assessments.add(assessment);
                allFulfillable &= assessment.isEnoughStock();

                if (!assessment.isEnoughStock() || assessment.isUsesNearExpiryLots()) {
                    riskyItemCount++;
                }
            }

            return new StaffOrderDetailView(
                    order,
                    assessments,
                    nearExpiryDays,
                    allFulfillable,
                    riskyItemCount);
        });
    }

    private FefoAllocationPlan buildPlanForOrderItem(jakarta.persistence.EntityManager em,
            OrderItem item,
            LocalDate today,
            int nearExpiryDays) {
        List<OrderItemLotReservation> reservations = reservationRepository.findActiveByOrderItemId(em, item.getId());
        if (reservations != null && !reservations.isEmpty()) {
            return buildReservedPlan(reservations, item.getQuantity(), today, nearExpiryDays);
        }

        List<ProductLot> lots = lotRepo.findAvailableLotsFEFO(em, item.getProduct().getId(), today);
        return OrderFefoPlanner.buildPlan(lots, item.getQuantity(), today, nearExpiryDays);
    }

    private FefoAllocationPlan buildReservedPlan(List<OrderItemLotReservation> reservations,
            int requestedQty,
            LocalDate today,
            int nearExpiryDays) {
        List<FefoAllocationLot> allocations = new ArrayList<>();
        int safeRequestedQty = Math.max(0, requestedQty);
        int availableQty = 0;
        int nearExpiryQty = 0;
        boolean usesNearExpiryLots = false;
        LocalDate nearestExpiry = null;
        int remaining = safeRequestedQty;

        for (OrderItemLotReservation reservation : reservations) {
            if (reservation == null || reservation.getProductLot() == null) {
                continue;
            }

            ProductLot lot = reservation.getProductLot();
            Integer reservedQty = reservation.getReservedQty();
            int reserved = reservedQty == null ? 0 : Math.max(0, reservedQty);
            if (reserved <= 0) {
                continue;
            }

            LocalDate expiry = lot.getExpiryDate();
            if (expiry != null && (nearestExpiry == null || expiry.isBefore(nearestExpiry))) {
                nearestExpiry = expiry;
            }

            boolean validForCompletion = expiry != null && !expiry.isBefore(today);
            boolean nearExpiry = validForCompletion && FEFOUtil.needsUrgentUse(lot, today, nearExpiryDays);
            if (validForCompletion) {
                availableQty += reserved;
                if (nearExpiry) {
                    nearExpiryQty += reserved;
                    usesNearExpiryLots = true;
                }
            }

            int take = 0;
            if (validForCompletion && remaining > 0) {
                take = Math.min(remaining, reserved);
                remaining -= take;
            }

            long daysUntilExpiry = expiry == null ? Long.MIN_VALUE : FEFOUtil.getDaysUntilExpiry(lot, today);
            allocations.add(new FefoAllocationLot(
                    lot.getId(),
                    lot.getImportDate(),
                    expiry,
                    reserved,
                    take,
                    daysUntilExpiry,
                    nearExpiry));
        }

        int shortageQty = Math.max(0, safeRequestedQty - availableQty);
        return new FefoAllocationPlan(
                safeRequestedQty,
                availableQty,
                nearExpiryQty,
                shortageQty,
                nearestExpiry,
                shortageQty == 0,
                usesNearExpiryLots,
                allocations);
    }
}

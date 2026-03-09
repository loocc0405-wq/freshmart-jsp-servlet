package com.freshmart.service;

import com.freshmart.entity.Order;
import com.freshmart.entity.OrderItem;
import com.freshmart.entity.ProductLot;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.OrderRepository;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.FefoAllocationPlan;
import com.freshmart.service.dto.StaffOrderDetailView;
import com.freshmart.service.dto.StaffOrderItemAssessment;
import com.freshmart.service.dto.StaffOrderListRow;
import com.freshmart.util.JpaExecutor;
import com.freshmart.util.OrderFefoPlanner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffOrderOmsService {

    public static final int DEFAULT_LIST_LIMIT = 50;
    public static final int DEFAULT_NEAR_EXPIRY_DAYS = 3;

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderRepository orderRepo = new OrderRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();

    public List<StaffOrderListRow> listOrders(OrderStatus status, int limit, int nearExpiryDays) {
        return executor.execute(em -> {
            List<Order> orders = orderRepo.findForStaffList(em, status, Math.max(1, limit));
            LocalDate today = LocalDate.now();
            List<StaffOrderListRow> rows = new ArrayList<>();

            for (Order order : orders) {
                boolean inventoryCheckRelevant =
                        order.getStatus() == OrderStatus.PENDING
                        || order.getStatus() == OrderStatus.PROCESSING
                        || order.getStatus() == OrderStatus.SHIPPING;

                boolean fulfillable = true;
                boolean hasNearExpiry = false;

                if (inventoryCheckRelevant) {
                    for (OrderItem item : order.getItems()) {
                        List<ProductLot> lots = lotRepo.findAvailableLotsFEFO(em, item.getProduct().getId(), today);
                        FefoAllocationPlan plan = OrderFefoPlanner.buildPlan(
                                lots,
                                item.getQuantity(),
                                today,
                                nearExpiryDays
                        );
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

            LocalDate today = LocalDate.now();
            List<StaffOrderItemAssessment> assessments = new ArrayList<>();
            boolean allFulfillable = true;
            int riskyItemCount = 0;

            for (OrderItem item : order.getItems()) {
                List<ProductLot> lots = lotRepo.findAvailableLotsFEFO(em, item.getProduct().getId(), today);
                FefoAllocationPlan plan = OrderFefoPlanner.buildPlan(
                        lots,
                        item.getQuantity(),
                        today,
                        nearExpiryDays
                );

                StaffOrderItemAssessment assessment = new StaffOrderItemAssessment(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        plan
                );

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
                    riskyItemCount
            );
        });
    }
}

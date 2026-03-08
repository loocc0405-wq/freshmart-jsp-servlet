package com.freshmart.service;

import com.freshmart.entity.Order;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.OrderRepository;
import com.freshmart.service.dto.CustomerDashboardSummary;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CustomerDashboardService {

    private static final BigDecimal DEFAULT_SPENDING_ALERT_THRESHOLD = new BigDecimal("5000000");

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderRepository orderRepository = new OrderRepository();

    public CustomerDashboardSummary getSummary(Long customerId) {
        return executor.execute(em -> {
            List<Order> allOrders = orderRepository.findByCustomerId(em, customerId);

            BigDecimal totalSpent = orderRepository.getTotalSpentByCustomer(em, customerId);
            BigDecimal spentLast30Days = orderRepository.getTotalSpentByCustomerSince(
                    em,
                    customerId,
                    LocalDateTime.now().minusDays(30)
            );
            BigDecimal averageCompletedOrderAmount = orderRepository.getAverageCompletedOrderAmount(em, customerId);
            Optional<Order> latestCompletedOrder = orderRepository.findLatestCompletedByCustomer(em, customerId);

            long totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING)
                    .count();
            long completedOrders = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .count();

            CustomerDashboardSummary summary = new CustomerDashboardSummary();
            summary.setTotalOrders(totalOrders);
            summary.setPendingOrders(pendingOrders);
            summary.setCompletedOrders(completedOrders);
            summary.setTotalSpent(totalSpent);
            summary.setSpentLast30Days(spentLast30Days);
            summary.setAverageCompletedOrderAmount(averageCompletedOrderAmount);
            summary.setLatestCompletedOrderAmount(
                    latestCompletedOrder.map(Order::getTotalAmount).orElse(BigDecimal.ZERO)
            );
            summary.setLatestCompletedAt(
                    latestCompletedOrder.map(Order::getCompletedAt).orElse(null)
            );
            summary.setSpendingAlertThreshold(DEFAULT_SPENDING_ALERT_THRESHOLD);
            summary.setOverSpendingThreshold(
                    spentLast30Days.compareTo(DEFAULT_SPENDING_ALERT_THRESHOLD) >= 0
            );

            return summary;
        });
    }
}
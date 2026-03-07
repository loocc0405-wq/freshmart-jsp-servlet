package com.freshmart.service;

import com.freshmart.entity.Order;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.OrderRepository;
import com.freshmart.service.dto.CustomerDashboardSummary;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.util.List;

public class CustomerDashboardService {

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderRepository orderRepository = new OrderRepository();

    public CustomerDashboardSummary getSummary(Long customerId) {
        return executor.execute(em -> {
            List<Order> allOrders = orderRepository.findByCustomerId(em, customerId);
            BigDecimal totalSpent = orderRepository.getTotalSpentByCustomer(em, customerId);

            long totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING)
                    .count();

            long completedOrders = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .count();

            return new CustomerDashboardSummary(
                    totalOrders,
                    pendingOrders,
                    completedOrders,
                    totalSpent
            );
        });
    }
}
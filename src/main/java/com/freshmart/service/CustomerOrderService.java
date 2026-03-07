package com.freshmart.service;

import com.freshmart.entity.Order;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.OrderRepository;
import com.freshmart.util.JpaExecutor;

import java.util.List;

public class CustomerOrderService {

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderRepository orderRepository = new OrderRepository();

    public List<Order> getOrdersByCustomer(Long customerId) {
        return executor.execute(em -> orderRepository.findByCustomerId(em, customerId));
    }

    public List<Order> getOrdersByCustomerAndStatus(Long customerId, String status) {
        if (status == null || status.isBlank()) {
            return getOrdersByCustomer(customerId);
        }

        OrderStatus orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
        return executor.execute(em -> orderRepository.findByCustomerIdAndStatus(em, customerId, orderStatus));
    }

    public Order getOrderDetail(Long customerId, Long orderId) {
        return executor.execute(em ->
                orderRepository.findByIdAndCustomerId(em, orderId, customerId)
                        .orElseThrow(() -> new IllegalArgumentException("Order not found"))
        );
    }
}
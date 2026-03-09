package com.freshmart.service;

import com.freshmart.entity.Order;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.OrderRepository;
import com.freshmart.util.JpaExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CustomerOrderService {

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderRepository orderRepository = new OrderRepository();

    public List<Order> getOrdersByCustomer(Long customerId) {
        return executor.execute(em -> orderRepository.findByCustomerId(em, customerId));
    }

    public List<Order> getOrdersByCustomerAndStatus(Long customerId, String status) {
        OrderStatus orderStatus = parseStatus(status);
        if (orderStatus == null) {
            return getOrdersByCustomer(customerId);
        }

        return executor.execute(em -> orderRepository.findByCustomerIdAndStatus(em, customerId, orderStatus));
    }

    public List<Order> getOrdersByFilters(Long customerId,
                                          String status,
                                          String fromDate,
                                          String toDate,
                                          int page,
                                          int size) {
        return executor.execute(em -> {
            OrderStatus orderStatus = parseStatus(status);
            LocalDateTime from = parseFromDate(fromDate);
            LocalDateTime to = parseToDate(toDate);

            return orderRepository.findByCustomerWithFilters(
                    em,
                    customerId,
                    orderStatus,
                    from,
                    to,
                    page,
                    size
            );
        });
    }

    public long countOrdersByFilters(Long customerId,
                                     String status,
                                     String fromDate,
                                     String toDate) {
        return executor.execute(em -> {
            OrderStatus orderStatus = parseStatus(status);
            LocalDateTime from = parseFromDate(fromDate);
            LocalDateTime to = parseToDate(toDate);

            return orderRepository.countByCustomerWithFilters(
                    em,
                    customerId,
                    orderStatus,
                    from,
                    to
            );
        });
    }

    public Order getOrderDetail(Long customerId, Long orderId) {
        return executor.execute(em ->
                orderRepository.findByIdAndCustomerId(em, orderId, customerId)
                        .orElseThrow(() -> new IllegalArgumentException("Order not found"))
        );
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String normalized = status.trim().toUpperCase();

        // hỗ trợ cả 2 cách viết
        if ("CANCELLED".equals(normalized)) {
            normalized = "CANCELED";
        }

        try {
            return OrderStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDateTime parseFromDate(String fromDate) {
        if (fromDate == null || fromDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(fromDate).atStartOfDay();
    }

    private LocalDateTime parseToDate(String toDate) {
        if (toDate == null || toDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(toDate).atTime(LocalTime.MAX);
    }
}
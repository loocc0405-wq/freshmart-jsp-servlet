package com.freshmart.repository;

import com.freshmart.entity.OrderItemLotAllocation;
import jakarta.persistence.EntityManager;

import java.util.List;

public class OrderItemLotAllocationRepository {

    public OrderItemLotAllocation save(EntityManager em, OrderItemLotAllocation allocation) {
        if (allocation.getId() == null) {
            em.persist(allocation);
            return allocation;
        }
        return em.merge(allocation);
    }

    public List<OrderItemLotAllocation> findByOrderId(EntityManager em, Long orderId) {
        return em.createQuery(
                "SELECT a FROM OrderItemLotAllocation a " +
                        "JOIN FETCH a.orderItem oi " +
                        "JOIN FETCH a.productLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE oi.order.id = :orderId " +
                        "ORDER BY oi.id ASC, l.expiryDate ASC, l.importDate ASC, l.id ASC",
                OrderItemLotAllocation.class
        ).setParameter("orderId", orderId).getResultList();
    }
}

package com.freshmart.repository;

import com.freshmart.entity.OrderItemLotReservation;
import jakarta.persistence.EntityManager;

import java.util.List;

public class OrderItemLotReservationRepository {

    public OrderItemLotReservation save(EntityManager em, OrderItemLotReservation reservation) {
        if (reservation.getId() == null) {
            em.persist(reservation);
            return reservation;
        }
        return em.merge(reservation);
    }

    public List<OrderItemLotReservation> findActiveByOrderId(EntityManager em, Long orderId) {
        return em.createQuery(
                "SELECT r FROM OrderItemLotReservation r " +
                        "JOIN FETCH r.orderItem oi " +
                        "JOIN FETCH r.productLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE oi.order.id = :orderId AND r.releasedAt IS NULL " +
                        "ORDER BY oi.id ASC, l.expiryDate ASC, l.importDate ASC, l.id ASC",
                OrderItemLotReservation.class).setParameter("orderId", orderId).getResultList();
    }

    public List<OrderItemLotReservation> findActiveByOrderItemId(EntityManager em, Long orderItemId) {
        return em.createQuery(
                "SELECT r FROM OrderItemLotReservation r " +
                        "JOIN FETCH r.productLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE r.orderItem.id = :orderItemId AND r.releasedAt IS NULL " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                OrderItemLotReservation.class).setParameter("orderItemId", orderItemId).getResultList();
    }

    public long countActiveByOrderItemId(EntityManager em, Long orderItemId) {
        Long count = em.createQuery(
                "SELECT COUNT(r) FROM OrderItemLotReservation r " +
                        "WHERE r.orderItem.id = :orderItemId AND r.releasedAt IS NULL",
                Long.class).setParameter("orderItemId", orderItemId).getSingleResult();
        return count == null ? 0L : count;
    }
}

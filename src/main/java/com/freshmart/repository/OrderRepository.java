package com.freshmart.repository;

import com.freshmart.entity.Order;
import com.freshmart.enums.OrderStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class OrderRepository {

    public Optional<Order> findById(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(Order.class, id));
    }

    public Order save(EntityManager em, Order order) {
        if (order.getId() == null) {
            em.persist(order);
            return order;
        }
        return em.merge(order);
    }

    public List<Order> listRecent(EntityManager em, int limit) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o ORDER BY o.createdAt DESC",
                Order.class
        );
        q.setMaxResults(limit);
        return q.getResultList();
    }

    public List<Order> listByStatus(EntityManager em, OrderStatus status, int limit) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o WHERE o.status = :st ORDER BY o.createdAt DESC",
                Order.class
        );
        q.setParameter("st", status);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    public List<Order> findByCustomerId(EntityManager em, Long customerId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items " +
                "WHERE o.customer.id = :customerId " +
                "ORDER BY o.createdAt DESC",
                Order.class
        );
        q.setParameter("customerId", customerId);
        return q.getResultList();
    }

    public List<Order> findByCustomerIdAndStatus(EntityManager em, Long customerId, OrderStatus status) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items " +
                "WHERE o.customer.id = :customerId AND o.status = :status " +
                "ORDER BY o.createdAt DESC",
                Order.class
        );
        q.setParameter("customerId", customerId);
        q.setParameter("status", status);
        return q.getResultList();
    }

    public Optional<Order> findByIdAndCustomerId(EntityManager em, Long orderId, Long customerId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.product " +
                "WHERE o.id = :orderId AND o.customer.id = :customerId",
                Order.class
        );
        q.setParameter("orderId", orderId);
        q.setParameter("customerId", customerId);

        List<Order> result = q.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public BigDecimal getTotalSpentByCustomer(EntityManager em, Long customerId) {
        TypedQuery<BigDecimal> q = em.createQuery(
                "SELECT COALESCE(SUM(o.totalAmount), 0) " +
                "FROM Order o " +
                "WHERE o.customer.id = :customerId AND o.status = :status",
                BigDecimal.class
        );
        q.setParameter("customerId", customerId);
        q.setParameter("status", OrderStatus.COMPLETED);

        BigDecimal result = q.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public long countOrdersByCustomer(EntityManager em, Long customerId) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId",
                Long.class
        );
        q.setParameter("customerId", customerId);
        return q.getSingleResult();
    }
}
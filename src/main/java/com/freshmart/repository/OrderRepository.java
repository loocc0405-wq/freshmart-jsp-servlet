package com.freshmart.repository;

import com.freshmart.entity.Order;
import com.freshmart.enums.OrderStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
}

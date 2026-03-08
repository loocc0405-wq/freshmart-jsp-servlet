package com.freshmart.repository;

import com.freshmart.entity.SubscriptionPayment;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class SubscriptionPaymentRepository {

    public SubscriptionPayment save(EntityManager em, SubscriptionPayment payment) {
        if (payment.getId() == null) {
            em.persist(payment);
            return payment;
        }
        return em.merge(payment);
    }

    public List<SubscriptionPayment> findAll(EntityManager em) {
        return em.createQuery(
                        "SELECT p FROM SubscriptionPayment p JOIN FETCH p.user u ORDER BY p.createdAt DESC, p.id DESC",
                        SubscriptionPayment.class)
                .getResultList();
    }

    public List<SubscriptionPayment> findByUserId(EntityManager em, Long userId) {
        return em.createQuery(
                        "SELECT p FROM SubscriptionPayment p JOIN FETCH p.user u " +
                                "WHERE u.id = :userId ORDER BY p.createdAt DESC, p.id DESC",
                        SubscriptionPayment.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Optional<SubscriptionPayment> findById(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(SubscriptionPayment.class, id));
    }
}
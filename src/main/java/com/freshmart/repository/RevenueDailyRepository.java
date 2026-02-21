package com.freshmart.repository;

import com.freshmart.entity.RevenueDaily;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RevenueDailyRepository {

    public Optional<RevenueDaily> findByDate(EntityManager em, LocalDate date) {
        return Optional.ofNullable(em.find(RevenueDaily.class, date));
    }

    public List<RevenueDaily> findBetween(EntityManager em, LocalDate from, LocalDate toInclusive) {
        TypedQuery<RevenueDaily> q = em.createQuery(
                "SELECT r FROM RevenueDaily r WHERE r.revenueDate BETWEEN :from AND :to ORDER BY r.revenueDate ASC",
                RevenueDaily.class
        );
        q.setParameter("from", from);
        q.setParameter("to", toInclusive);
        return q.getResultList();
    }

    public RevenueDaily save(EntityManager em, RevenueDaily revenue) {
        return em.merge(revenue);
    }
}

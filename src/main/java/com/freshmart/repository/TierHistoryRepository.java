package com.freshmart.repository;

import com.freshmart.entity.TierHistory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TierHistoryRepository {

    public TierHistory save(EntityManager em, TierHistory history) {
        if (history.getId() == null) {
            em.persist(history);
            return history;
        }
        return em.merge(history);
    }

    public List<TierHistory> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT h FROM TierHistory h JOIN FETCH h.user u ORDER BY h.createdAt DESC, h.id DESC",
                TierHistory.class)
                .getResultList();
    }

    public List<TierHistory> findByUserId(EntityManager em, Long userId) {
        return em.createQuery(
                "SELECT h FROM TierHistory h JOIN FETCH h.user u " +
                        "WHERE u.id = :userId ORDER BY h.createdAt DESC, h.id DESC",
                TierHistory.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}

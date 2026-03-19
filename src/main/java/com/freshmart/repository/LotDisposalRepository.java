package com.freshmart.repository;

import com.freshmart.entity.LotDisposal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class LotDisposalRepository {

    public LotDisposal save(EntityManager em, LotDisposal disposal) {
        if (disposal.getId() == null) {
            em.persist(disposal);
            return disposal;
        }
        return em.merge(disposal);
    }

    public List<LotDisposal> findRecent(EntityManager em, int limit) {
        TypedQuery<LotDisposal> query = em.createQuery(
                "SELECT d FROM LotDisposal d " +
                        "JOIN FETCH d.productLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "LEFT JOIN FETCH d.disposedBy u " +
                        "ORDER BY d.disposedAt DESC, d.id DESC",
                LotDisposal.class
        );
        query.setMaxResults(Math.max(1, limit));
        return query.getResultList();
    }
}

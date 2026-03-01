package com.freshmart.repository;

import com.freshmart.entity.ProductLot;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

public class ProductLotRepository {

    /**
     * FEFO: first expired, first out.
     * Only take lots with qtyLeft > 0 and not expired.
     */
    public List<ProductLot> findAvailableLotsFEFO(EntityManager em, Long productId, LocalDate today) {
        TypedQuery<ProductLot> q = em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "WHERE l.product.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class
        );
        q.setParameter("pid", productId);
        q.setParameter("today", today);
        return q.getResultList();
    }


    public java.time.LocalDate findNearestExpiry(EntityManager em, Long productId, LocalDate today) {
        return em.createQuery(
                        "SELECT MIN(l.expiryDate) FROM ProductLot l " +
                                "WHERE l.product.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today",
                        java.time.LocalDate.class
                )
                .setParameter("pid", productId)
                .setParameter("today", today)
                .getSingleResult();
    }

    public int getAvailableQty(EntityManager em, Long productId, LocalDate today) {
        Long sum = em.createQuery(
                        "SELECT COALESCE(SUM(l.qtyLeft), 0) FROM ProductLot l " +
                                "WHERE l.product.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today",
                        Long.class
                )
                .setParameter("pid", productId)
                .setParameter("today", today)
                .getSingleResult();
        return sum.intValue();
    }
}

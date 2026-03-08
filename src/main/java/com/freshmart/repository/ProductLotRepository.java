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
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE p.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class
        );
        q.setParameter("pid", productId);
        q.setParameter("today", today);
        return q.getResultList();
    }

    /**
     * FEFO with pessimistic write lock to prevent race conditions during stock consumption.
     * Used within a transaction to ensure exclusive access to lots.
     */
    public List<ProductLot> findAvailableLotsFEFOForUpdate(EntityManager em, Long productId, LocalDate today) {
        TypedQuery<ProductLot> q = em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE p.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today " +
                        "ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC",
                ProductLot.class
        );
        q.setParameter("pid", productId);
        q.setParameter("today", today);
        q.setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return q.getResultList();
    }

    /**
     * Nearest expiry date among non-expired lots with qtyLeft > 0.
     * Returns null if no valid lots.
     */
    public LocalDate findNearestExpiry(EntityManager em, Long productId, LocalDate today) {
        List<LocalDate> res = em.createQuery(
                        "SELECT MIN(l.expiryDate) FROM ProductLot l " +
                                "WHERE l.product.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today",
                        LocalDate.class
                )
                .setParameter("pid", productId)
                .setParameter("today", today)
                .getResultList();

        return (res == null || res.isEmpty()) ? null : res.get(0);
    }

    /**
     * Total qty available (qtyLeft) for non-expired lots.
     */
    public int getAvailableQty(EntityManager em, Long productId, LocalDate today) {
        Long sum = em.createQuery(
                        "SELECT COALESCE(SUM(l.qtyLeft), 0) FROM ProductLot l " +
                                "WHERE l.product.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today",
                        Long.class
                )
                .setParameter("pid", productId)
                .setParameter("today", today)
                .getSingleResult();
        return sum == null ? 0 : sum.intValue();
    }

    /**
     * Total qtyLeft of lots that will expire within [today .. today+days] (inclusive),
     * and still have qtyLeft > 0.
     */
    public int getExpiringQty(EntityManager em, Long productId, LocalDate today, int days) {
        if (days < 0) days = 0;
        LocalDate end = today.plusDays(days);

        Long sum = em.createQuery(
                        "SELECT COALESCE(SUM(l.qtyLeft), 0) FROM ProductLot l " +
                                "WHERE l.product.id = :pid AND l.qtyLeft > 0 " +
                                "AND l.expiryDate >= :today AND l.expiryDate <= :end",
                        Long.class
                )
                .setParameter("pid", productId)
                .setParameter("today", today)
                .setParameter("end", end)
                .getSingleResult();

        return sum == null ? 0 : sum.intValue();
    }

    /**
     * Count number of lots that will expire within [today .. today+days] (inclusive),
     * and still have qtyLeft > 0.
     */
    public int countExpiringLots(EntityManager em, Long productId, LocalDate today, int days) {
        if (days < 0) days = 0;
        LocalDate end = today.plusDays(days);

        Long cnt = em.createQuery(
                        "SELECT COUNT(l) FROM ProductLot l " +
                                "WHERE l.product.id = :pid AND l.qtyLeft > 0 " +
                                "AND l.expiryDate >= :today AND l.expiryDate <= :end",
                        Long.class
                )
                .setParameter("pid", productId)
                .setParameter("today", today)
                .setParameter("end", end)
                .getSingleResult();

        return cnt == null ? 0 : cnt.intValue();
    }

    public Integer findSuggestedLeadTimeDays(EntityManager em, Long productId) {
        List<Integer> result = em.createQuery(
                        "SELECT s.leadTimeDays " +
                        "FROM ProductLot l " +
                        "JOIN l.supplier s " +
                        "WHERE l.product.id = :pid " +
                        "AND s.leadTimeDays IS NOT NULL " +
                        "ORDER BY l.importDate DESC, l.id DESC",
                        Integer.class
                )
                .setParameter("pid", productId)
                .setMaxResults(1)
                .getResultList();

        if (result == null || result.isEmpty() || result.get(0) == null || result.get(0) <= 0) {
            return 1;
        }
        return result.get(0);
    }
}
package com.freshmart.repository;

import com.freshmart.entity.ProductLot;
import com.freshmart.service.dto.InventoryLotFilter;
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
        return null;
    }
    return result.get(0);
}

    /**
     * Find a lot by ID with eagerly loaded product and supplier references.
     * Safe for editing operations.
     */
    public ProductLot findByIdWithRefs(EntityManager em, Long lotId) {
        List<ProductLot> list = em.createQuery(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE l.id = :id",
                ProductLot.class
        ).setParameter("id", lotId).getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Search lots with dynamic filtering based on InventoryLotFilter conditions.
     */
    public List<ProductLot> searchLots(EntityManager em, InventoryLotFilter filter, LocalDate today) {
        StringBuilder jpql = new StringBuilder(
                "SELECT l FROM ProductLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "WHERE 1 = 1 "
        );

        if (filter.getProductId() != null) {
            jpql.append("AND p.id = :productId ");
        }

        if (filter.getSupplierId() != null) {
            jpql.append("AND s.id = :supplierId ");
        }

        if (filter.getImportFrom() != null) {
            jpql.append("AND l.importDate >= :importFrom ");
        }

        if (filter.getImportTo() != null) {
            jpql.append("AND l.importDate <= :importTo ");
        }

        if (filter.getExpiryFrom() != null) {
            jpql.append("AND l.expiryDate >= :expiryFrom ");
        }

        if (filter.getExpiryTo() != null) {
            jpql.append("AND l.expiryDate <= :expiryTo ");
        }

        if (filter.getMinQtyLeft() != null) {
            jpql.append("AND l.qtyLeft >= :minQtyLeft ");
        }

        if (filter.getMaxQtyLeft() != null) {
            jpql.append("AND l.qtyLeft <= :maxQtyLeft ");
        }

        if ("AVAILABLE".equalsIgnoreCase(filter.getStatus())) {
            jpql.append("AND l.qtyLeft > 0 AND l.expiryDate >= :today ");
        } else if ("EXPIRING".equalsIgnoreCase(filter.getStatus())) {
            jpql.append("AND l.qtyLeft > 0 AND l.expiryDate >= :today AND l.expiryDate <= :expiringDeadline ");
        } else if ("EXPIRED".equalsIgnoreCase(filter.getStatus())) {
            jpql.append("AND l.qtyLeft > 0 AND l.expiryDate < :today ");
        } else if ("CONSUMED".equalsIgnoreCase(filter.getStatus())) {
            jpql.append("AND l.qtyLeft = 0 ");
        }

        jpql.append("ORDER BY l.expiryDate ASC, l.importDate ASC, l.id ASC");

        TypedQuery<ProductLot> query = em.createQuery(jpql.toString(), ProductLot.class);

        if (filter.getProductId() != null) {
            query.setParameter("productId", filter.getProductId());
        }

        if (filter.getSupplierId() != null) {
            query.setParameter("supplierId", filter.getSupplierId());
        }

        if (filter.getImportFrom() != null) {
            query.setParameter("importFrom", filter.getImportFrom());
        }

        if (filter.getImportTo() != null) {
            query.setParameter("importTo", filter.getImportTo());
        }

        if (filter.getExpiryFrom() != null) {
            query.setParameter("expiryFrom", filter.getExpiryFrom());
        }

        if (filter.getExpiryTo() != null) {
            query.setParameter("expiryTo", filter.getExpiryTo());
        }

        if (filter.getMinQtyLeft() != null) {
            query.setParameter("minQtyLeft", filter.getMinQtyLeft());
        }

        if (filter.getMaxQtyLeft() != null) {
            query.setParameter("maxQtyLeft", filter.getMaxQtyLeft());
        }

        if ("AVAILABLE".equalsIgnoreCase(filter.getStatus())
                || "EXPIRING".equalsIgnoreCase(filter.getStatus())
                || "EXPIRED".equalsIgnoreCase(filter.getStatus())) {
            query.setParameter("today", today);
        }

        if ("EXPIRING".equalsIgnoreCase(filter.getStatus())) {
            query.setParameter("expiringDeadline", today.plusDays(7));
        }

        return query.getResultList();
    }

    /**
     * Count lots matching the filter conditions (for summary statistics).
     */
    public long countLots(EntityManager em, InventoryLotFilter filter, LocalDate today) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(l) FROM ProductLot l " +
                        "JOIN l.product p " +
                        "LEFT JOIN l.supplier s " +
                        "WHERE 1 = 1 "
        );

        if (filter.getProductId() != null) {
            jpql.append("AND p.id = :productId ");
        }

        if (filter.getSupplierId() != null) {
            jpql.append("AND s.id = :supplierId ");
        }

        if (filter.getImportFrom() != null) {
            jpql.append("AND l.importDate >= :importFrom ");
        }

        if (filter.getImportTo() != null) {
            jpql.append("AND l.importDate <= :importTo ");
        }

        if (filter.getExpiryFrom() != null) {
            jpql.append("AND l.expiryDate >= :expiryFrom ");
        }

        if (filter.getExpiryTo() != null) {
            jpql.append("AND l.expiryDate <= :expiryTo ");
        }

        if (filter.getMinQtyLeft() != null) {
            jpql.append("AND l.qtyLeft >= :minQtyLeft ");
        }

        if (filter.getMaxQtyLeft() != null) {
            jpql.append("AND l.qtyLeft <= :maxQtyLeft ");
        }

        if ("AVAILABLE".equalsIgnoreCase(filter.getStatus())) {
            jpql.append("AND l.qtyLeft > 0 AND l.expiryDate >= :today ");
        } else if ("EXPIRING".equalsIgnoreCase(filter.getStatus())) {
            jpql.append("AND l.qtyLeft > 0 AND l.expiryDate >= :today AND l.expiryDate <= :expiringDeadline ");
        } else if ("EXPIRED".equalsIgnoreCase(filter.getStatus())) {
            jpql.append("AND l.qtyLeft > 0 AND l.expiryDate < :today ");
        } else if ("CONSUMED".equalsIgnoreCase(filter.getStatus())) {
            jpql.append("AND l.qtyLeft = 0 ");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        if (filter.getProductId() != null) {
            query.setParameter("productId", filter.getProductId());
        }

        if (filter.getSupplierId() != null) {
            query.setParameter("supplierId", filter.getSupplierId());
        }

        if (filter.getImportFrom() != null) {
            query.setParameter("importFrom", filter.getImportFrom());
        }

        if (filter.getImportTo() != null) {
            query.setParameter("importTo", filter.getImportTo());
        }

        if (filter.getExpiryFrom() != null) {
            query.setParameter("expiryFrom", filter.getExpiryFrom());
        }

        if (filter.getExpiryTo() != null) {
            query.setParameter("expiryTo", filter.getExpiryTo());
        }

        if (filter.getMinQtyLeft() != null) {
            query.setParameter("minQtyLeft", filter.getMinQtyLeft());
        }

        if (filter.getMaxQtyLeft() != null) {
            query.setParameter("maxQtyLeft", filter.getMaxQtyLeft());
        }

        if ("AVAILABLE".equalsIgnoreCase(filter.getStatus())
                || "EXPIRING".equalsIgnoreCase(filter.getStatus())
                || "EXPIRED".equalsIgnoreCase(filter.getStatus())) {
            query.setParameter("today", today);
        }

        if ("EXPIRING".equalsIgnoreCase(filter.getStatus())) {
            query.setParameter("expiringDeadline", today.plusDays(7));
        }

        return query.getSingleResult();
    }
}
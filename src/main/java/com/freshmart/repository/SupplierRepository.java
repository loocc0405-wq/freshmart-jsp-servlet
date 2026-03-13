package com.freshmart.repository;

import com.freshmart.entity.Supplier;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SupplierRepository {
    public List<Supplier> findAll(EntityManager em) {
        return em.createQuery("SELECT s FROM Supplier s ORDER BY s.id ASC", Supplier.class).getResultList();
    }
    public Optional<Supplier> findById(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(Supplier.class, id));
    }
    public Supplier save(EntityManager em, Supplier s) {
        if (s.getId() == null) { em.persist(s); return s; }
        return em.merge(s);
    }
    public void deleteById(EntityManager em, Long id) {
        Supplier s = em.find(Supplier.class, id);
        if (s != null) em.remove(s);
    }

    public Optional<Supplier> findByEmail(EntityManager em, String email) {
        try {
            Supplier s = em.createQuery("SELECT s FROM Supplier s WHERE s.email = :email", Supplier.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(s);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Search suppliers with optional keyword and certificate filter, with paging.
     * keyword will be matched against name, email or phone (case‑insensitive, partial).
     */
    public List<Supplier> search(EntityManager em,
                                 String keyword,
                                 String certificate,
                                 LocalDateTime fromDate,
                                 LocalDateTime toDate,
                                 int offset,
                                 int limit) {
        StringBuilder jpql = new StringBuilder("SELECT s FROM Supplier s");
        StringBuilder where = new StringBuilder();

        if (keyword != null && !keyword.isEmpty()) {
            where.append(" (LOWER(s.name) LIKE :kw OR LOWER(s.email) LIKE :kw OR LOWER(s.phone) LIKE :kw)");
        }
        if (certificate != null && !certificate.isEmpty()) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append(" s.certificate = :cert");
        }
        if (fromDate != null) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            // match either creation or last update
            where.append(" (s.createdAt >= :fromDate OR s.updatedAt >= :fromDate)");
        }
        if (toDate != null) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append(" (s.createdAt <= :toDate OR s.updatedAt <= :toDate)");
        }
        if (where.length() > 0) {
            jpql.append(" WHERE").append(where);
        }
        jpql.append(" ORDER BY s.id ASC");

        var query = em.createQuery(jpql.toString(), Supplier.class);
        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        }
        if (certificate != null && !certificate.isEmpty()) {
            query.setParameter("cert", certificate);
        }
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public long count(EntityManager em, String keyword, String certificate, LocalDateTime fromDate, LocalDateTime toDate) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(s) FROM Supplier s");
        StringBuilder where = new StringBuilder();
        if (keyword != null && !keyword.isEmpty()) {
            where.append(" (LOWER(s.name) LIKE :kw OR LOWER(s.email) LIKE :kw OR LOWER(s.phone) LIKE :kw)");
        }
        if (certificate != null && !certificate.isEmpty()) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append(" s.certificate = :cert");
        }
        if (fromDate != null) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append(" (s.createdAt >= :fromDate OR s.updatedAt >= :fromDate)");
        }
        if (toDate != null) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append(" (s.createdAt <= :toDate OR s.updatedAt <= :toDate)");
        }
        if (where.length() > 0) {
            jpql.append(" WHERE").append(where);
        }
        var query = em.createQuery(jpql.toString(), Long.class);
        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        }
        if (certificate != null && !certificate.isEmpty()) {
            query.setParameter("cert", certificate);
        }
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        return query.getSingleResult();
    }

    // -------------------------------------------------------------
    // statistics helpers (used by SupplierService)
    // -------------------------------------------------------------

    /**
     * Total count of suppliers (ignores filters).
     */
    public long countAll(EntityManager em) {
        return em.createQuery("SELECT COUNT(s) FROM Supplier s", Long.class)
                .getSingleResult();
    }

    /**
     * Count suppliers that either have a non-empty certificate or not.
     */
    public long countByCertificate(EntityManager em, boolean hasCertificate) {
        String jpql = hasCertificate
                ? "SELECT COUNT(s) FROM Supplier s WHERE s.certificate IS NOT NULL AND s.certificate <> ''"
                : "SELECT COUNT(s) FROM Supplier s WHERE s.certificate IS NULL OR s.certificate = ''";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    /**
     * Average lead time (in days) among all suppliers. Returns null when there are no rows.
     */
    public Double averageLeadTime(EntityManager em) {
        return em.createQuery("SELECT AVG(s.leadTimeDays) FROM Supplier s", Double.class)
                .getSingleResult();
    }

    /**
     * Top suppliers ordered by number of distinct products supplied (via product lots).
     * Each element of the result list is an Object[]{ Supplier, Long count }.
     */
    public List<Object[]> topSuppliersByProductCount(EntityManager em, int limit) {
        String jpql = "SELECT s, COUNT(DISTINCT l.product) " +
                "FROM ProductLot l JOIN l.supplier s " +
                "WHERE l.supplier IS NOT NULL " +
                "GROUP BY s " +
                "ORDER BY COUNT(DISTINCT l.product) DESC";
        var query = em.createQuery(jpql, Object[].class);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * Get supplier scorecard statistics.
     * Returns Object[] with: supplierId, totalLots, totalQtyIn, totalImportValue, 
     * distinctProducts, nearExpiryLots, expiredLots, lastImportDate
     */
    public List<Object[]> getSupplierScorecardStats(EntityManager em) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate nearExpiryThreshold = today.plusDays(7);
        
        String jpql = "SELECT l.supplier.id, " +
                "COUNT(l), " +
                "SUM(l.qtyIn), " +
                "SUM(l.qtyIn * l.importPrice), " +
                "COUNT(DISTINCT l.product), " +
                "SUM(CASE WHEN l.expiryDate > :today AND l.expiryDate <= :nearExpiry THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN l.expiryDate <= :today THEN 1 ELSE 0 END), " +
                "MAX(l.importDate) " +
                "FROM ProductLot l " +
                "WHERE l.supplier IS NOT NULL " +
                "GROUP BY l.supplier.id";
        return em.createQuery(jpql, Object[].class)
                .setParameter("today", today)
                .setParameter("nearExpiry", nearExpiryThreshold)
                .getResultList();
    }
}

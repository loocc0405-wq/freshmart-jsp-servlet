package com.freshmart.repository;

import com.freshmart.entity.Supplier;

import jakarta.persistence.EntityManager;
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

    /**
     * Search suppliers with optional keyword and certificate filter, with paging.
     * keyword will be matched against name, email or phone (case‑insensitive, partial).
     */
    public List<Supplier> search(EntityManager em,
                                 String keyword,
                                 String certificate,
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
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public long count(EntityManager em, String keyword, String certificate) {
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
        return query.getSingleResult();
    }
}

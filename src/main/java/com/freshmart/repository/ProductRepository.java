package com.freshmart.repository;

import com.freshmart.entity.Product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    public List<Product> findAll(EntityManager em, boolean showInactive) {
        // show oldest first (ascending id)
        String jpql = "SELECT p FROM Product p";
        if (!showInactive) {
            jpql += " WHERE p.active = true";
        }
        jpql += " ORDER BY p.id ASC";
        return em.createQuery(jpql, Product.class)
                .getResultList();
    }

    public Optional<Product> findById(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(Product.class, id));
    }

    public List<Product> search(EntityManager em, String keyword, String category, boolean showInactive) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");
        if (!showInactive) {
            jpql.append(" AND p.active = true");
        }
        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" AND LOWER(p.name) LIKE :kw");
        }
        if (category != null && !category.isBlank()) {
            jpql.append(" AND p.category = :cat");
        }
        jpql.append(" ORDER BY p.id ASC");

        TypedQuery<Product> q = em.createQuery(jpql.toString(), Product.class);
        if (keyword != null && !keyword.isBlank()) q.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        if (category != null && !category.isBlank()) q.setParameter("cat", category);
        return q.getResultList();
    }

    // NEW: dùng để đổ dropdown category
    public List<String> listCategories(EntityManager em) {
        return em.createQuery(
                        "SELECT DISTINCT p.category FROM Product p " +
                                "WHERE p.category IS NOT NULL AND p.category <> '' " +
                                "ORDER BY p.category",
                        String.class)
                .getResultList();
    }

    public Product save(EntityManager em, Product p) {
        if (p.getId() == null) {
            em.persist(p);
            return p;
        }
        return em.merge(p);
    }

    public void deleteById(EntityManager em, Long id) {
        Product p = em.find(Product.class, id);
        if (p != null) em.remove(p);
    }

    // Pagination support
    public List<Product> findAllPaginated(EntityManager em, boolean showInactive, int offset, int limit) {
        String jpql = "SELECT p FROM Product p";
        if (!showInactive) {
            jpql += " WHERE p.active = true";
        }
        jpql += " ORDER BY p.id ASC";
        
        return em.createQuery(jpql, Product.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countAll(EntityManager em, boolean showInactive) {
        String jpql = "SELECT COUNT(p) FROM Product p";
        if (!showInactive) {
            jpql += " WHERE p.active = true";
        }
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public List<Product> searchPaginated(EntityManager em, String keyword, String category, 
                                         boolean showInactive, int offset, int limit) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");
        if (!showInactive) {
            jpql.append(" AND p.active = true");
        }
        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" AND LOWER(p.name) LIKE :kw");
        }
        if (category != null && !category.isBlank()) {
            jpql.append(" AND p.category = :cat");
        }
        jpql.append(" ORDER BY p.id ASC");

        TypedQuery<Product> q = em.createQuery(jpql.toString(), Product.class);
        if (keyword != null && !keyword.isBlank()) q.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        if (category != null && !category.isBlank()) q.setParameter("cat", category);
        
        return q.setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countSearch(EntityManager em, String keyword, String category, boolean showInactive) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM Product p WHERE 1=1");
        if (!showInactive) {
            jpql.append(" AND p.active = true");
        }
        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" AND LOWER(p.name) LIKE :kw");
        }
        if (category != null && !category.isBlank()) {
            jpql.append(" AND p.category = :cat");
        }

        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        if (keyword != null && !keyword.isBlank()) q.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        if (category != null && !category.isBlank()) q.setParameter("cat", category);
        
        return q.getSingleResult();
    }
}
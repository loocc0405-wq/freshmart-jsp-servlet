package com.freshmart.repository;

import com.freshmart.entity.Product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    public List<Product> findAll(EntityManager em) {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.id DESC", Product.class)
                .getResultList();
    }

    public Optional<Product> findById(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(Product.class, id));
    }

    public List<Product> search(EntityManager em, String keyword, String category) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" AND LOWER(p.name) LIKE :kw ");
        }
        if (category != null && !category.isBlank()) {
            jpql.append(" AND p.category = :cat ");
        }
        jpql.append(" ORDER BY p.id DESC");

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
}
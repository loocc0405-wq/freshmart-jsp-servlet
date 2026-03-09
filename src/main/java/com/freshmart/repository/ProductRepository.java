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

    public List<Product> search(EntityManager em, String keyword, String category, String stockStatus, boolean showInactive) {
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
        
        List<Product> results = q.getResultList();
        
        // Apply stock status filter in memory
        if (stockStatus != null && !stockStatus.isBlank() && !"all".equals(stockStatus)) {
            java.time.LocalDate today = java.time.LocalDate.now();
            results = results.stream()
                    .filter(p -> {
                        int availableQty = getAvailableQty(em, p.getId(), today);
                        if ("inStock".equals(stockStatus)) {
                            return availableQty > 0;
                        } else if ("outOfStock".equals(stockStatus)) {
                            return availableQty == 0;
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return results;
    }
    
    private int getAvailableQty(EntityManager em, Long productId, java.time.LocalDate today) {
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

    public List<Product> searchPaginated(EntityManager em, String keyword, String category, String stockStatus,
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
        
        List<Product> results = q.setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
        
        // Apply stock status filter in memory
        if (stockStatus != null && !stockStatus.isBlank() && !"all".equals(stockStatus)) {
            java.time.LocalDate today = java.time.LocalDate.now();
            results = results.stream()
                    .filter(p -> {
                        int availableQty = getAvailableQty(em, p.getId(), today);
                        if ("inStock".equals(stockStatus)) {
                            return availableQty > 0;
                        } else if ("outOfStock".equals(stockStatus)) {
                            return availableQty == 0;
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return results;
    }

    public long countSearch(EntityManager em, String keyword, String category, String stockStatus, boolean showInactive) {
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
        
        long count = q.getSingleResult();
        
        // If stock status filter is applied, we need to count filtered results
        if (stockStatus != null && !stockStatus.isBlank() && !"all".equals(stockStatus)) {
            // Get all products matching other criteria
            jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");
            if (!showInactive) {
                jpql.append(" AND p.active = true");
            }
            if (keyword != null && !keyword.isBlank()) {
                jpql.append(" AND LOWER(p.name) LIKE :kw");
            }
            if (category != null && !category.isBlank()) {
                jpql.append(" AND p.category = :cat");
            }
            
            TypedQuery<Product> pq = em.createQuery(jpql.toString(), Product.class);
            if (keyword != null && !keyword.isBlank()) pq.setParameter("kw", "%" + keyword.toLowerCase() + "%");
            if (category != null && !category.isBlank()) pq.setParameter("cat", category);
            
            List<Product> allProducts = pq.getResultList();
            java.time.LocalDate today = java.time.LocalDate.now();
            
            count = allProducts.stream()
                    .filter(p -> {
                        int availableQty = getAvailableQty(em, p.getId(), today);
                        if ("inStock".equals(stockStatus)) {
                            return availableQty > 0;
                        } else if ("outOfStock".equals(stockStatus)) {
                            return availableQty == 0;
                        }
                        return true;
                    })
                    .count();
        }
        
        return count;
    }
}
package com.freshmart.repository;

import com.freshmart.entity.Product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductRepository {

    public List<Product> findAll(EntityManager em, boolean showInactive) {
        String jpql = "SELECT p FROM Product p";
        if (!showInactive) {
            jpql += " WHERE p.active = true";
        }
        jpql += " ORDER BY p.id ASC";
        return em.createQuery(jpql, Product.class).getResultList();
    }

    public Optional<Product> findById(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(Product.class, id));
    }

    public List<Product> search(EntityManager em,
                                String keyword,
                                String category,
                                String stockStatus,
                                boolean showInactive) {
        List<Product> baseResults = loadBaseSearchResults(em, keyword, category, showInactive);
        return filterByStockStatus(em, baseResults, stockStatus);
    }

    private int getAvailableQty(EntityManager em, Long productId, LocalDate today) {
        Long sum = em.createQuery(
                "SELECT COALESCE(SUM(l.qtyLeft), 0) FROM ProductLot l " +
                        "WHERE l.product.id = :pid AND l.qtyLeft > 0 AND l.expiryDate >= :today",
                Long.class
        ).setParameter("pid", productId)
                .setParameter("today", today)
                .getSingleResult();
        return sum == null ? 0 : sum.intValue();
    }

    public List<String> listCategories(EntityManager em) {
        return em.createQuery(
                "SELECT DISTINCT p.category FROM Product p " +
                        "WHERE p.category IS NOT NULL AND p.category <> '' " +
                        "ORDER BY p.category",
                String.class
        ).getResultList();
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
        if (p != null) {
            em.remove(p);
        }
    }

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

    public List<Product> searchPaginated(EntityManager em,
                                         String keyword,
                                         String category,
                                         String stockStatus,
                                         boolean showInactive,
                                         int offset,
                                         int limit) {
        List<Product> filtered = search(em, keyword, category, stockStatus, showInactive);
        if (filtered.isEmpty()) {
            return List.of();
        }

        int safeOffset = Math.max(0, offset);
        if (safeOffset >= filtered.size()) {
            return List.of();
        }

        int safeLimit = Math.max(0, limit);
        int toIndex = Math.min(filtered.size(), safeOffset + safeLimit);
        return filtered.subList(safeOffset, toIndex);
    }

    public long countSearch(EntityManager em,
                            String keyword,
                            String category,
                            String stockStatus,
                            boolean showInactive) {
        List<Product> filtered = search(em, keyword, category, stockStatus, showInactive);
        return filtered.size();
    }

    private List<Product> loadBaseSearchResults(EntityManager em,
                                                String keyword,
                                                String category,
                                                boolean showInactive) {
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

        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        if (keyword != null && !keyword.isBlank()) {
            query.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        }
        if (category != null && !category.isBlank()) {
            query.setParameter("cat", category);
        }

        return query.getResultList();
    }

    private List<Product> filterByStockStatus(EntityManager em,
                                              List<Product> products,
                                              String stockStatus) {
        if (stockStatus == null || stockStatus.isBlank() || "all".equalsIgnoreCase(stockStatus)) {
            return products;
        }

        LocalDate today = LocalDate.now();
        return products.stream()
                .filter(product -> matchesStockStatus(em, product, stockStatus, today))
                .collect(Collectors.toList());
    }

    private boolean matchesStockStatus(EntityManager em,
                                       Product product,
                                       String stockStatus,
                                       LocalDate today) {
        int availableQty = getAvailableQty(em, product.getId(), today);

        if ("inStock".equalsIgnoreCase(stockStatus)) {
            return availableQty > 0;
        }
        if ("outOfStock".equalsIgnoreCase(stockStatus)) {
            return availableQty == 0;
        }
        return true;
    }
}
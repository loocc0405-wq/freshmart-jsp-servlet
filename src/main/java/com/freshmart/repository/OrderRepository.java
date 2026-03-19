package com.freshmart.repository;

import com.freshmart.entity.Order;
import com.freshmart.enums.OrderStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OrderRepository {

    public Optional<Order> findById(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(Order.class, id));
    }

    public Optional<Order> findByIdForUpdate(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(Order.class, id, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE));
    }

    public Order save(EntityManager em, Order order) {
        if (order.getId() == null) {
            em.persist(order);
            return order;
        }
        return em.merge(order);
    }

    public List<Order> listRecent(EntityManager em, int limit) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o ORDER BY o.createdAt DESC",
                Order.class
        );
        q.setMaxResults(limit);
        return q.getResultList();
    }

    public List<Order> listByStatus(EntityManager em, OrderStatus status, int limit) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o WHERE o.status = :st ORDER BY o.createdAt DESC",
                Order.class
        );
        q.setParameter("st", status);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    public List<Order> findByCustomerId(EntityManager em, Long customerId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items " +
                "WHERE o.customer.id = :customerId " +
                "ORDER BY o.createdAt DESC",
                Order.class
        );
        q.setParameter("customerId", customerId);
        return q.getResultList();
    }

    public List<Order> findByCustomerIdAndStatus(EntityManager em, Long customerId, OrderStatus status) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items " +
                "WHERE o.customer.id = :customerId AND o.status = :status " +
                "ORDER BY o.createdAt DESC",
                Order.class
        );
        q.setParameter("customerId", customerId);
        q.setParameter("status", status);
        return q.getResultList();
    }

    public List<Order> findByCustomerWithFilters(
            EntityManager em,
            Long customerId,
            OrderStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    ) {
        StringBuilder jpql = new StringBuilder(
                "SELECT o FROM Order o WHERE o.customer.id = :customerId "
        );

        if (status != null) {
            jpql.append("AND o.status = :status ");
        }
        if (fromDate != null) {
            jpql.append("AND o.createdAt >= :fromDate ");
        }
        if (toDate != null) {
            jpql.append("AND o.createdAt <= :toDate ");
        }

        jpql.append("ORDER BY o.createdAt DESC");

        TypedQuery<Order> q = em.createQuery(jpql.toString(), Order.class);
        q.setParameter("customerId", customerId);

        if (status != null) {
            q.setParameter("status", status);
        }
        if (fromDate != null) {
            q.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            q.setParameter("toDate", toDate);
        }

        q.setFirstResult(Math.max(0, page) * size);
        q.setMaxResults(size);

        return q.getResultList();
    }

    public long countByCustomerWithFilters(
            EntityManager em,
            Long customerId,
            OrderStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId "
        );

        if (status != null) {
            jpql.append("AND o.status = :status ");
        }
        if (fromDate != null) {
            jpql.append("AND o.createdAt >= :fromDate ");
        }
        if (toDate != null) {
            jpql.append("AND o.createdAt <= :toDate ");
        }

        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        q.setParameter("customerId", customerId);

        if (status != null) {
            q.setParameter("status", status);
        }
        if (fromDate != null) {
            q.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            q.setParameter("toDate", toDate);
        }

        return q.getSingleResult();
    }

    public Optional<Order> findByIdAndCustomerId(EntityManager em, Long orderId, Long customerId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.product " +
                "WHERE o.id = :orderId AND o.customer.id = :customerId",
                Order.class
        );
        q.setParameter("orderId", orderId);
        q.setParameter("customerId", customerId);

        List<Order> result = q.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public BigDecimal getTotalSpentByCustomer(EntityManager em, Long customerId) {
        TypedQuery<BigDecimal> q = em.createQuery(
                "SELECT COALESCE(SUM(o.totalAmount), 0) " +
                "FROM Order o " +
                "WHERE o.customer.id = :customerId AND o.status = :status",
                BigDecimal.class
        );
        q.setParameter("customerId", customerId);
        q.setParameter("status", OrderStatus.COMPLETED);

        BigDecimal result = q.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getTotalSpentByCustomerSince(EntityManager em, Long customerId, LocalDateTime since) {
        TypedQuery<BigDecimal> q = em.createQuery(
                "SELECT COALESCE(SUM(o.totalAmount), 0) " +
                "FROM Order o " +
                "WHERE o.customer.id = :customerId " +
                "AND o.status = :status " +
                "AND o.createdAt >= :since",
                BigDecimal.class
        );
        q.setParameter("customerId", customerId);
        q.setParameter("status", OrderStatus.COMPLETED);
        q.setParameter("since", since);

        BigDecimal result = q.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getAverageCompletedOrderAmount(EntityManager em, Long customerId) {
        TypedQuery<Double> q = em.createQuery(
                "SELECT AVG(o.totalAmount) " +
                "FROM Order o " +
                "WHERE o.customer.id = :customerId AND o.status = :status",
                Double.class
        );
        q.setParameter("customerId", customerId);
        q.setParameter("status", OrderStatus.COMPLETED);

        Double result = q.getSingleResult();
        return result != null ? BigDecimal.valueOf(result) : BigDecimal.ZERO;
    }

    public Optional<Order> findLatestCompletedByCustomer(EntityManager em, Long customerId) {
        TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o " +
                "WHERE o.customer.id = :customerId AND o.status = :status " +
                "ORDER BY o.completedAt DESC, o.createdAt DESC",
                Order.class
        );
        q.setParameter("customerId", customerId);
        q.setParameter("status", OrderStatus.COMPLETED);
        q.setMaxResults(1);

        List<Order> result = q.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public long countOrdersByCustomer(EntityManager em, Long customerId) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId",
                Long.class
        );
        q.setParameter("customerId", customerId);
        return q.getSingleResult();
    }

    public List<Order> findForStaffList(EntityManager em, OrderStatus status, int limit) {
        String base = "SELECT o.id FROM Order o ";
        if (status != null) {
            base += "WHERE o.status = :st ";
        }
        base += "ORDER BY o.createdAt DESC";

        TypedQuery<Long> idQuery = em.createQuery(base, Long.class);
        if (status != null) {
            idQuery.setParameter("st", status);
        }
        idQuery.setMaxResults(limit);
        List<Long> ids = idQuery.getResultList();
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Order> orders = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.customer " +
                "LEFT JOIN FETCH o.createdBy " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.product " +
                "WHERE o.id IN :ids",
                Order.class
        ).setParameter("ids", ids).getResultList();

        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return orders;
    }

    public Optional<Order> findByIdWithRefs(EntityManager em, Long orderId) {
        List<Order> result = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.customer " +
                "LEFT JOIN FETCH o.createdBy " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.product " +
                "WHERE o.id = :id",
                Order.class
        ).setParameter("id", orderId).getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
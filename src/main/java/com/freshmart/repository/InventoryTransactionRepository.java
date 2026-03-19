package com.freshmart.repository;

import com.freshmart.entity.InventoryTransaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class InventoryTransactionRepository {

    public InventoryTransaction save(EntityManager em, InventoryTransaction transaction) {
        if (transaction.getId() == null) {
            em.persist(transaction);
            return transaction;
        }
        return em.merge(transaction);
    }

    public List<InventoryTransaction> findRecent(EntityManager em, int limit) {
        TypedQuery<InventoryTransaction> query = em.createQuery(
                "SELECT t FROM InventoryTransaction t " +
                        "JOIN FETCH t.productLot l " +
                        "JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH l.supplier s " +
                        "LEFT JOIN FETCH t.createdBy u " +
                        "ORDER BY t.createdAt DESC, t.id DESC",
                InventoryTransaction.class
        );
        query.setMaxResults(Math.max(1, limit));
        return query.getResultList();
    }
}

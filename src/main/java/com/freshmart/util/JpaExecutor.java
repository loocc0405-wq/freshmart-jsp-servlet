package com.freshmart.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Function;

/**
 * Helper to run a unit of work with automatic transaction/rollback.
 */
public class JpaExecutor {

    public <T> T execute(Function<EntityManager, T> work) {
        EntityManager entityManager = JPAUtil.createEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            T result = work.apply(entityManager);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            entityManager.close();
        }
    }

    public void executeVoid(java.util.function.Consumer<EntityManager> work) {
        execute(em -> {
            work.accept(em);
            return null;
        });
    }
}

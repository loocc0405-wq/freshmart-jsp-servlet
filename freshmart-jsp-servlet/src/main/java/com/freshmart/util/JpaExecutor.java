package com.freshmart.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.function.Function;

/**
 * Helper to run a unit of work with automatic transaction/rollback.
 */
public class JpaExecutor {

    public <T> T execute(Function<EntityManager, T> work) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void executeVoid(java.util.function.Consumer<EntityManager> work) {
        execute(em -> {
            work.accept(em);
            return null;
        });
    }
}

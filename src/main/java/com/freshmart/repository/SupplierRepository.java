package com.freshmart.repository;

import com.freshmart.entity.Supplier;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class SupplierRepository {
    public List<Supplier> findAll(EntityManager em) {
        return em.createQuery("SELECT s FROM Supplier s ORDER BY s.id DESC", Supplier.class).getResultList();
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
}

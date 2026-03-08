package com.freshmart.repository;

import com.freshmart.entity.User;
import com.freshmart.enums.Role;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByUsername(EntityManager em, String username) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username AND u.active = true",
                User.class
        );
        q.setParameter("username", username);
        List<User> rs = q.getResultList();
        return rs.isEmpty() ? Optional.empty() : Optional.of(rs.get(0));
    }

    public List<User> findByRole(EntityManager em, Role role) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role ORDER BY u.id DESC",
                User.class
        );
        q.setParameter("role", role);
        return q.getResultList();
    }

    public List<User> findCustomers(EntityManager em) {
        return em.createQuery(
                        "SELECT u FROM User u WHERE u.role = :role ORDER BY u.id DESC",
                        User.class)
                .setParameter("role", Role.CUSTOMER)
                .getResultList();
    }

    public boolean existsByUsername(EntityManager em, String username) {
        Long c = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username",
                Long.class
        ).setParameter("username", username)
         .getSingleResult();
        return c != null && c > 0;
    }

    public long count(EntityManager em) {
        return em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
    }

    public User save(EntityManager em, User user) {
        if (user.getId() == null) {
            em.persist(user);
            return user;
        }
        return em.merge(user);
    }

    public Optional<User> findById(EntityManager em, Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }
}
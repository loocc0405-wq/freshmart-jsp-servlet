package com.freshmart.repository;

import com.freshmart.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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

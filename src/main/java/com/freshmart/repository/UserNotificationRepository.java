package com.freshmart.repository;

import com.freshmart.entity.UserNotification;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class UserNotificationRepository {

    public UserNotification save(EntityManager em, UserNotification notification) {
        if (notification.getId() == null) {
            em.persist(notification);
            return notification;
        }
        return em.merge(notification);
    }

    public boolean existsByUniqueKey(EntityManager em, String uniqueKey) {
        Long count = em.createQuery(
                        "SELECT COUNT(n) FROM UserNotification n WHERE n.uniqueKey = :uniqueKey",
                        Long.class)
                .setParameter("uniqueKey", uniqueKey)
                .getSingleResult();
        return count != null && count > 0;
    }

    public List<UserNotification> findRecentByUserIdAndCategory(EntityManager em, Long userId, String category, int limit) {
        return em.createQuery(
                        "SELECT n FROM UserNotification n " +
                                "WHERE n.user.id = :userId AND n.category = :category " +
                                "ORDER BY n.createdAt DESC, n.id DESC",
                        UserNotification.class)
                .setParameter("userId", userId)
                .setParameter("category", category)
                .setMaxResults(Math.max(limit, 1))
                .getResultList();
    }

    public long countUnreadByUserIdAndCategory(EntityManager em, Long userId, String category) {
        Long count = em.createQuery(
                        "SELECT COUNT(n) FROM UserNotification n " +
                                "WHERE n.user.id = :userId AND n.category = :category AND n.read = false",
                        Long.class)
                .setParameter("userId", userId)
                .setParameter("category", category)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    public int markAllReadByUserIdAndCategory(EntityManager em, Long userId, String category, LocalDateTime readAt) {
        return em.createQuery(
                        "UPDATE UserNotification n SET n.read = true, n.readAt = :readAt " +
                                "WHERE n.user.id = :userId AND n.category = :category AND n.read = false")
                .setParameter("readAt", readAt)
                .setParameter("userId", userId)
                .setParameter("category", category)
                .executeUpdate();
    }
}

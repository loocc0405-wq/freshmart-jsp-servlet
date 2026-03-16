package com.freshmart.service;

import com.freshmart.entity.User;
import com.freshmart.entity.UserNotification;
import com.freshmart.repository.UserNotificationRepository;
import com.freshmart.util.JpaExecutor;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserNotificationService {

    public static final String CATEGORY_SUBSCRIPTION = "SUBSCRIPTION";
    public static final String TYPE_EXPIRING_SOON = "SUB_EXPIRING_SOON";
    public static final String TYPE_EXPIRED_IN_GRACE = "SUB_EXPIRED_IN_GRACE";
    public static final String TYPE_EXPIRED = "SUB_EXPIRED";

    private final JpaExecutor executor = new JpaExecutor();
    private final UserNotificationRepository repository = new UserNotificationRepository();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public int createSubscriptionNotificationForStatus(EntityManager em, User user,
                                                       com.freshmart.service.dto.SubscriptionStatusDTO status,
                                                       LocalDate today) {
        if (user == null || status == null || user.getExpiredDate() == null) {
            return 0;
        }

        switch (status.getStatus()) {
            case com.freshmart.service.dto.SubscriptionStatusDTO.PRO_EXPIRING_SOON:
                return createIfAbsent(em, user,
                        TYPE_EXPIRING_SOON,
                        today,
                        "PRO sắp hết hạn",
                        "Gói PRO của bạn sẽ hết hạn sau " + status.getDaysRemaining()
                                + " ngày, vào ngày " + formatDate(user.getExpiredDate())
                                + ". Hãy gia hạn sớm để tránh bị hạ về FREE.",
                        TYPE_EXPIRING_SOON + ":" + user.getId() + ":" + user.getExpiredDate() + ":" + today);
            case com.freshmart.service.dto.SubscriptionStatusDTO.PRO_EXPIRED_IN_GRACE:
                return createIfAbsent(em, user,
                        TYPE_EXPIRED_IN_GRACE,
                        today,
                        "PRO đã hết hạn - còn grace period",
                        "Gói PRO của bạn đã hết hạn từ ngày " + formatDate(user.getExpiredDate())
                                + ". Bạn còn " + status.getGraceRemaining()
                                + " ngày grace period để gia hạn và giữ quyền truy cập.",
                        TYPE_EXPIRED_IN_GRACE + ":" + user.getId() + ":" + user.getExpiredDate() + ":" + today);
            case com.freshmart.service.dto.SubscriptionStatusDTO.PRO_EXPIRED:
                return createIfAbsent(em, user,
                        TYPE_EXPIRED,
                        today,
                        "PRO đã hết hạn hoàn toàn",
                        "Gói PRO của bạn đã hết hạn hoàn toàn từ ngày " + formatDate(user.getExpiredDate())
                                + ". Hệ thống đã chuyển tài khoản của bạn về FREE. Hãy mua gói mới để kích hoạt lại.",
                        TYPE_EXPIRED + ":" + user.getId() + ":" + user.getExpiredDate());
            default:
                return 0;
        }
    }

    public List<UserNotification> getRecentSubscriptionNotifications(Long userId, int limit) {
        return executor.execute(em -> repository.findRecentByUserIdAndCategory(em, userId, CATEGORY_SUBSCRIPTION, limit));
    }

    public long countUnreadSubscriptionNotifications(Long userId) {
        return executor.execute(em -> repository.countUnreadByUserIdAndCategory(em, userId, CATEGORY_SUBSCRIPTION));
    }

    public int markAllSubscriptionRead(Long userId) {
        return executor.execute(em -> repository.markAllReadByUserIdAndCategory(
                em, userId, CATEGORY_SUBSCRIPTION, LocalDateTime.now()));
    }

    public void markAllSubscriptionRead(EntityManager em, Long userId) {
        repository.markAllReadByUserIdAndCategory(em, userId, CATEGORY_SUBSCRIPTION, LocalDateTime.now());
    }

    private int createIfAbsent(EntityManager em, User user, String type, LocalDate eventDate,
                               String title, String message, String uniqueKey) {
        if (repository.existsByUniqueKey(em, uniqueKey)) {
            return 0;
        }

        UserNotification notification = new UserNotification();
        notification.setUser(user);
        notification.setCategory(CATEGORY_SUBSCRIPTION);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setUniqueKey(uniqueKey);
        notification.setEventDate(eventDate);
        notification.setRead(false);
        repository.save(em, notification);
        return 1;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FMT.format(date);
    }
}

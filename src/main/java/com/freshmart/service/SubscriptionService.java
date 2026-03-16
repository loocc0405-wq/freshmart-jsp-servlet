package com.freshmart.service;

import com.freshmart.entity.SubscriptionPayment;
import com.freshmart.entity.TierHistory;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.enums.Tier;
import com.freshmart.repository.SubscriptionPaymentRepository;
import com.freshmart.repository.TierHistoryRepository;
import com.freshmart.repository.UserRepository;
import com.freshmart.service.dto.SubscriptionMaintenanceResult;
import com.freshmart.service.dto.SubscriptionStatusDTO;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SubscriptionService {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepo = new UserRepository();
    private final SubscriptionPaymentRepository paymentRepo = new SubscriptionPaymentRepository();
    private final TierHistoryRepository tierHistoryRepo = new TierHistoryRepository();
    private final AppSettingService appSettingService = new AppSettingService();
    private final UserNotificationService notificationService = new UserNotificationService();

    // ==================== Plan pricing ====================

    public Map<Integer, BigDecimal> getPlanPrices() {
        Map<Integer, BigDecimal> plans = new LinkedHashMap<>();
        plans.put(30, new BigDecimal("99000"));
        plans.put(90, new BigDecimal("249000"));
        plans.put(365, new BigDecimal("799000"));
        return plans;
    }

    public BigDecimal getPlanPrice(int days) {
        return getPlanPrices().getOrDefault(days, BigDecimal.valueOf(days * 3000L));
    }

    // ==================== Upgrade / Purchase ====================

    public User upgradePro(Long userId, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Số ngày phải > 0");
        }

        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            LocalDate today = LocalDate.now();
            normalizeTierInternal(u, today, em);

            Tier oldTier = u.getTier();
            LocalDate oldExpired = u.getExpiredDate();

            LocalDate base = (u.getExpiredDate() != null && !u.getExpiredDate().isBefore(today))
                    ? u.getExpiredDate()
                    : today;

            u.setTier(Tier.PRO);
            u.setExpiredDate(base.plusDays(days));

            User saved = userRepo.save(em, u);
            notificationService.markAllSubscriptionRead(em, saved.getId());

            String changeType = (oldTier == Tier.PRO) ? "RENEW" : "UPGRADE";
            recordTierHistory(em, saved, oldTier, Tier.PRO, oldExpired, saved.getExpiredDate(),
                    changeType, "Upgrade PRO " + days + " days");

            return saved;
        });
    }

    public SubscriptionPayment fakePurchase(Long userId, int days, String paymentMethod) {
        if (days <= 0) {
            throw new IllegalArgumentException("Số ngày phải > 0");
        }

        String method = (paymentMethod == null || paymentMethod.isBlank()) ? "FAKE_CARD" : paymentMethod.trim();
        BigDecimal amount = getPlanPrice(days);

        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

            if (u.getRole() != Role.CUSTOMER && u.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Chỉ customer/admin mới dùng subscription");
            }

            LocalDate today = LocalDate.now();
            normalizeTierInternal(u, today, em);

            Tier oldTier = u.getTier();
            LocalDate oldExpired = u.getExpiredDate();

            LocalDate base = (u.getExpiredDate() != null && !u.getExpiredDate().isBefore(today))
                    ? u.getExpiredDate()
                    : today;

            LocalDate newExpiredDate = base.plusDays(days);

            u.setTier(Tier.PRO);
            u.setExpiredDate(newExpiredDate);
            userRepo.save(em, u);
            notificationService.markAllSubscriptionRead(em, u.getId());

            String changeType = (oldTier == Tier.PRO) ? "RENEW" : "UPGRADE";
            recordTierHistory(em, u, oldTier, Tier.PRO, oldExpired, newExpiredDate,
                    changeType, "Fake payment " + method + " - PRO " + days + " days");

            SubscriptionPayment payment = new SubscriptionPayment();
            payment.setUser(u);
            payment.setPaymentCode(generatePaymentCode());
            payment.setPlanName("PRO " + days + " days");
            payment.setPlanDays(days);
            payment.setAmount(amount);
            payment.setPaymentMethod(method);
            payment.setPaymentStatus("SUCCESS");
            payment.setStartDate(today);
            payment.setEndDate(newExpiredDate);
            payment.setNote("Fake payment from upgrade UI");

            return paymentRepo.save(em, payment);
        });
    }

    public SubscriptionPayment adminGrant(Long userId, int days, String note) {
        if (days <= 0) {
            throw new IllegalArgumentException("Số ngày phải > 0");
        }

        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

            LocalDate today = LocalDate.now();
            normalizeTierInternal(u, today, em);

            Tier oldTier = u.getTier();
            LocalDate oldExpired = u.getExpiredDate();

            LocalDate base = (u.getExpiredDate() != null && !u.getExpiredDate().isBefore(today))
                    ? u.getExpiredDate()
                    : today;

            LocalDate newExpiredDate = base.plusDays(days);

            u.setTier(Tier.PRO);
            u.setExpiredDate(newExpiredDate);
            userRepo.save(em, u);
            notificationService.markAllSubscriptionRead(em, u.getId());

            recordTierHistory(em, u, oldTier, Tier.PRO, oldExpired, newExpiredDate,
                    "ADMIN_GRANT", note == null || note.isBlank() ? "Admin grant PRO" : note);

            SubscriptionPayment payment = new SubscriptionPayment();
            payment.setUser(u);
            payment.setPaymentCode(generatePaymentCode());
            payment.setPlanName("ADMIN GRANT " + days + " days");
            payment.setPlanDays(days);
            payment.setAmount(BigDecimal.ZERO);
            payment.setPaymentMethod("ADMIN_GRANT");
            payment.setPaymentStatus("SUCCESS");
            payment.setStartDate(today);
            payment.setEndDate(newExpiredDate);
            payment.setNote(note == null || note.isBlank() ? "Admin grant PRO" : note);

            return paymentRepo.save(em, payment);
        });
    }

    // ==================== Revoke PRO ====================

    public User revokePro(Long userId, String note) {
        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

            Tier oldTier = u.getTier();
            LocalDate oldExpired = u.getExpiredDate();

            u.setTier(Tier.FREE);
            u.setExpiredDate(null);
            User saved = userRepo.save(em, u);
            notificationService.markAllSubscriptionRead(em, saved.getId());

            recordTierHistory(em, saved, oldTier, Tier.FREE, oldExpired, null,
                    "ADMIN_REVOKE", note == null || note.isBlank() ? "Admin revoke PRO" : note);

            return saved;
        });
    }

    // ==================== Refresh / Sync ====================

    public User refreshAndSync(Long userId) {
        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

            normalizeTierInternal(u, LocalDate.now(), em);
            return userRepo.save(em, u);
        });
    }

    public SubscriptionMaintenanceResult runMaintenanceSweep() {
        int notifyDays = appSettingService.getSubNotifyDays();
        int graceDays = appSettingService.getSubGracePeriodDays();
        LocalDate today = LocalDate.now();

        return executor.execute(em -> {
            int scanned = 0;
            int downgraded = 0;
            int createdNotifications = 0;

            List<User> candidates = userRepo.findSubscriptionCandidates(em);
            for (User user : candidates) {
                scanned++;

                if (SubscriptionPolicy.shouldAutoDowngrade(user, today)) {
                    normalizeTierInternal(user, today, em);
                    userRepo.save(em, user);
                    downgraded++;
                }

                SubscriptionStatusDTO status = SubscriptionPolicy.computeStatus(user, today, notifyDays, graceDays);
                createdNotifications += notificationService.createSubscriptionNotificationForStatus(em, user, status, today);
            }

            return new SubscriptionMaintenanceResult(today, scanned, downgraded, createdNotifications);
        });
    }

    // ==================== Query methods ====================

    public List<User> getCustomerUsers() {
        return executor.execute(userRepo::findCustomers);
    }

    public List<SubscriptionPayment> getAllPayments() {
        return executor.execute(paymentRepo::findAll);
    }

    public List<SubscriptionPayment> getPaymentsByUser(Long userId) {
        return executor.execute(em -> paymentRepo.findByUserId(em, userId));
    }

    public List<TierHistory> getAllTierHistory() {
        return executor.execute(tierHistoryRepo::findAll);
    }

    public List<TierHistory> getTierHistoryByUser(Long userId) {
        return executor.execute(em -> tierHistoryRepo.findByUserId(em, userId));
    }

    // ==================== Subscription Status ====================

    public SubscriptionStatusDTO computeStatus(User user) {
        int notifyDays = appSettingService.getSubNotifyDays();
        int graceDays = appSettingService.getSubGracePeriodDays();
        return SubscriptionPolicy.computeStatus(user, LocalDate.now(), notifyDays, graceDays);
    }

    public SubscriptionStatusDTO computeStatus(User user, LocalDate today, int notifyDays, int graceDays) {
        return SubscriptionPolicy.computeStatus(user, today, notifyDays, graceDays);
    }

    // ==================== Internal helpers ====================

    private void normalizeTierInternal(User u, LocalDate today, jakarta.persistence.EntityManager em) {
        if (!SubscriptionPolicy.shouldAutoDowngrade(u, today)) {
            return;
        }

        Tier oldTier = u.getTier();
        LocalDate oldExpired = u.getExpiredDate();

        u.setTier(Tier.FREE);

        recordTierHistory(em, u, oldTier, Tier.FREE, oldExpired, null,
                "EXPIRE", "PRO hết hạn, tự động chuyển về FREE");
    }

    private void recordTierHistory(jakarta.persistence.EntityManager em,
                                   User user, Tier oldTier, Tier newTier,
                                   LocalDate oldExpired, LocalDate newExpired,
                                   String changeType, String note) {
        TierHistory history = new TierHistory();
        history.setUser(user);
        history.setOldTier(oldTier);
        history.setNewTier(newTier);
        history.setOldExpiredDate(oldExpired);
        history.setNewExpiredDate(newExpired);
        history.setChangeType(changeType);
        history.setNote(note);
        tierHistoryRepo.save(em, history);
    }

    private String generatePaymentCode() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100, 1000);
        return "SUB" + ts + rnd;
    }
}

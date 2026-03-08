package com.freshmart.service;

import com.freshmart.entity.SubscriptionPayment;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.enums.Tier;
import com.freshmart.repository.SubscriptionPaymentRepository;
import com.freshmart.repository.UserRepository;
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

    public User upgradePro(Long userId, int days) {
        if (days <= 0) throw new IllegalArgumentException("Số ngày phải > 0");

        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            LocalDate today = LocalDate.now();
            normalizeTierInternal(u, today);

            LocalDate base = (u.getExpiredDate() != null && !u.getExpiredDate().isBefore(today))
                    ? u.getExpiredDate()
                    : today;

            u.setTier(Tier.PRO);
            u.setExpiredDate(base.plusDays(days));

            return userRepo.save(em, u);
        });
    }

    public SubscriptionPayment fakePurchase(Long userId, int days, String paymentMethod) {
        if (days <= 0) throw new IllegalArgumentException("Số ngày phải > 0");

        String method = (paymentMethod == null || paymentMethod.isBlank()) ? "FAKE_CARD" : paymentMethod.trim();
        BigDecimal amount = getPlanPrice(days);

        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

            if (u.getRole() != Role.CUSTOMER && u.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Chỉ customer/admin mới dùng subscription");
            }

            LocalDate today = LocalDate.now();
            normalizeTierInternal(u, today);

            LocalDate base = (u.getExpiredDate() != null && !u.getExpiredDate().isBefore(today))
                    ? u.getExpiredDate()
                    : today;

            LocalDate newExpiredDate = base.plusDays(days);

            u.setTier(Tier.PRO);
            u.setExpiredDate(newExpiredDate);
            userRepo.save(em, u);

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
        if (days <= 0) throw new IllegalArgumentException("Số ngày phải > 0");

        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

            LocalDate today = LocalDate.now();
            normalizeTierInternal(u, today);

            LocalDate base = (u.getExpiredDate() != null && !u.getExpiredDate().isBefore(today))
                    ? u.getExpiredDate()
                    : today;

            LocalDate newExpiredDate = base.plusDays(days);

            u.setTier(Tier.PRO);
            u.setExpiredDate(newExpiredDate);
            userRepo.save(em, u);

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

    public User refreshAndSync(Long userId) {
        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

            normalizeTierInternal(u, LocalDate.now());
            return userRepo.save(em, u);
        });
    }

    public List<User> getCustomerUsers() {
        return executor.execute(userRepo::findCustomers);
    }

    public List<SubscriptionPayment> getAllPayments() {
        return executor.execute(paymentRepo::findAll);
    }

    public List<SubscriptionPayment> getPaymentsByUser(Long userId) {
        return executor.execute(em -> paymentRepo.findByUserId(em, userId));
    }

    private void normalizeTierInternal(User u, LocalDate today) {
        if (u.getRole() != Role.CUSTOMER) {
            return;
        }

        if (u.getTier() == Tier.PRO && (u.getExpiredDate() == null || u.getExpiredDate().isBefore(today))) {
            u.setTier(Tier.FREE);
        }
    }

    private String generatePaymentCode() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100, 1000);
        return "SUB" + ts + rnd;
    }
}
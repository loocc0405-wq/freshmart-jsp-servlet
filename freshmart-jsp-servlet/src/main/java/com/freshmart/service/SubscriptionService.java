package com.freshmart.service;

import com.freshmart.entity.User;
import com.freshmart.enums.Tier;
import com.freshmart.repository.UserRepository;
import com.freshmart.util.JpaExecutor;

import java.time.LocalDate;

public class SubscriptionService {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepo = new UserRepository();

    public User upgradePro(Long userId, int days) {
        if (days <= 0) throw new IllegalArgumentException("Số ngày phải > 0");

        return executor.execute(em -> {
            User u = userRepo.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            LocalDate today = LocalDate.now();
            LocalDate base = (u.getExpiredDate() != null && !u.getExpiredDate().isBefore(today))
                    ? u.getExpiredDate()
                    : today;

            u.setTier(Tier.PRO);
            u.setExpiredDate(base.plusDays(days));
            return userRepo.save(em, u);
        });
    }
}

package com.freshmart.service;

import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.enums.Tier;
import com.freshmart.service.dto.SubscriptionStatusDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class SubscriptionPolicy {

    private SubscriptionPolicy() {
    }

    public static SubscriptionStatusDTO computeStatus(User user, LocalDate today, int notifyDays, int graceDays) {
        if (user == null) {
            return new SubscriptionStatusDTO(SubscriptionStatusDTO.FREE, 0, 0, 0, notifyDays, graceDays);
        }

        if (user.getTier() != Tier.PRO && user.getExpiredDate() == null) {
            return new SubscriptionStatusDTO(SubscriptionStatusDTO.FREE, 0, 0, 0, notifyDays, graceDays);
        }

        LocalDate expDate = user.getExpiredDate();

        if (user.getTier() == Tier.PRO && expDate != null && !expDate.isBefore(today)) {
            long remaining = ChronoUnit.DAYS.between(today, expDate);
            if (remaining <= notifyDays) {
                return new SubscriptionStatusDTO(SubscriptionStatusDTO.PRO_EXPIRING_SOON,
                        remaining, 0, 0, notifyDays, graceDays);
            }
            return new SubscriptionStatusDTO(SubscriptionStatusDTO.PRO_ACTIVE,
                    remaining, 0, 0, notifyDays, graceDays);
        }

        if (expDate != null) {
            long daysExpired = ChronoUnit.DAYS.between(expDate, today);
            if (daysExpired <= 0) {
                return new SubscriptionStatusDTO(SubscriptionStatusDTO.PRO_EXPIRING_SOON,
                        0, 0, graceDays, notifyDays, graceDays);
            }
            long graceRemaining = graceDays - daysExpired;
            if (graceRemaining > 0) {
                return new SubscriptionStatusDTO(SubscriptionStatusDTO.PRO_EXPIRED_IN_GRACE,
                        0, daysExpired, graceRemaining, notifyDays, graceDays);
            }
            return new SubscriptionStatusDTO(SubscriptionStatusDTO.PRO_EXPIRED,
                    0, daysExpired, 0, notifyDays, graceDays);
        }

        return new SubscriptionStatusDTO(SubscriptionStatusDTO.FREE, 0, 0, 0, notifyDays, graceDays);
    }

    public static boolean shouldAutoDowngrade(User user, LocalDate today) {
        if (user == null || user.getRole() != Role.CUSTOMER) {
            return false;
        }
        if (user.getTier() != Tier.PRO) {
            return false;
        }
        return user.getExpiredDate() == null || user.getExpiredDate().isBefore(today);
    }
}

package com.freshmart.service;

import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.enums.Tier;
import com.freshmart.service.dto.SubscriptionStatusDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionPolicyTest {

    @Test
    void computeStatus_returnsActiveWhenProStillFarFromExpiry() {
        User user = new User("loc", "loc@example.com", "hash", Role.CUSTOMER);
        user.setTier(Tier.PRO);
        user.setExpiredDate(LocalDate.of(2026, 3, 25));

        SubscriptionStatusDTO status = SubscriptionPolicy.computeStatus(
                user,
                LocalDate.of(2026, 3, 1),
                7,
                3);

        assertEquals(SubscriptionStatusDTO.PRO_ACTIVE, status.getStatus());
        assertEquals(24, status.getDaysRemaining());
    }

    @Test
    void computeStatus_returnsExpiringSoonInsideNotifyWindow() {
        User user = new User("loc", "loc@example.com", "hash", Role.CUSTOMER);
        user.setTier(Tier.PRO);
        user.setExpiredDate(LocalDate.of(2026, 3, 5));

        SubscriptionStatusDTO status = SubscriptionPolicy.computeStatus(
                user,
                LocalDate.of(2026, 3, 1),
                7,
                3);

        assertEquals(SubscriptionStatusDTO.PRO_EXPIRING_SOON, status.getStatus());
        assertEquals(4, status.getDaysRemaining());
    }

    @Test
    void computeStatus_returnsExpiredInGraceEvenAfterTierNormalizedToFree() {
        User user = new User("loc", "loc@example.com", "hash", Role.CUSTOMER);
        user.setTier(Tier.FREE);
        user.setExpiredDate(LocalDate.of(2026, 2, 28));

        SubscriptionStatusDTO status = SubscriptionPolicy.computeStatus(
                user,
                LocalDate.of(2026, 3, 1),
                7,
                3);

        assertEquals(SubscriptionStatusDTO.PRO_EXPIRED_IN_GRACE, status.getStatus());
        assertEquals(1, status.getDaysExpired());
        assertEquals(2, status.getGraceRemaining());
    }

    @Test
    void computeStatus_returnsExpiredWhenGraceIsOver() {
        User user = new User("loc", "loc@example.com", "hash", Role.CUSTOMER);
        user.setTier(Tier.FREE);
        user.setExpiredDate(LocalDate.of(2026, 2, 20));

        SubscriptionStatusDTO status = SubscriptionPolicy.computeStatus(
                user,
                LocalDate.of(2026, 3, 1),
                7,
                3);

        assertEquals(SubscriptionStatusDTO.PRO_EXPIRED, status.getStatus());
        assertEquals(9, status.getDaysExpired());
        assertEquals(0, status.getGraceRemaining());
    }

    @Test
    void shouldAutoDowngrade_onlyForExpiredCustomerPro() {
        User customerPro = new User("loc", "loc@example.com", "hash", Role.CUSTOMER);
        customerPro.setTier(Tier.PRO);
        customerPro.setExpiredDate(LocalDate.of(2026, 2, 28));

        User adminPro = new User("admin", "admin@example.com", "hash", Role.ADMIN);
        adminPro.setTier(Tier.PRO);
        adminPro.setExpiredDate(LocalDate.of(2026, 2, 28));

        assertTrue(SubscriptionPolicy.shouldAutoDowngrade(customerPro, LocalDate.of(2026, 3, 1)));
        assertFalse(SubscriptionPolicy.shouldAutoDowngrade(adminPro, LocalDate.of(2026, 3, 1)));
    }
}

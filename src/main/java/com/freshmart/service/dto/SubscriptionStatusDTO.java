package com.freshmart.service.dto;

/**
 * Lightweight DTO holding computed subscription status for display/UX purposes.
 * Status values:
 * FREE, PRO_ACTIVE, PRO_EXPIRING_SOON, PRO_EXPIRED_IN_GRACE, PRO_EXPIRED
 */
public class SubscriptionStatusDTO {

    public static final String FREE = "FREE";
    public static final String PRO_ACTIVE = "PRO_ACTIVE";
    public static final String PRO_EXPIRING_SOON = "PRO_EXPIRING_SOON";
    public static final String PRO_EXPIRED_IN_GRACE = "PRO_EXPIRED_IN_GRACE";
    public static final String PRO_EXPIRED = "PRO_EXPIRED";

    private final String status;
    private final long daysRemaining; // days until expiry (0 if expired/free)
    private final long daysExpired; // days since expiry (0 if active/free)
    private final long graceRemaining; // grace days left (0 if not in grace)
    private final int notifyDays; // config value used
    private final int graceDays; // config value used

    public SubscriptionStatusDTO(String status, long daysRemaining, long daysExpired,
            long graceRemaining, int notifyDays, int graceDays) {
        this.status = status;
        this.daysRemaining = daysRemaining;
        this.daysExpired = daysExpired;
        this.graceRemaining = graceRemaining;
        this.notifyDays = notifyDays;
        this.graceDays = graceDays;
    }

    public String getStatus() {
        return status;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public long getDaysExpired() {
        return daysExpired;
    }

    public long getGraceRemaining() {
        return graceRemaining;
    }

    public int getNotifyDays() {
        return notifyDays;
    }

    public int getGraceDays() {
        return graceDays;
    }

    public boolean isFree() {
        return FREE.equals(status);
    }

    public boolean isProActive() {
        return PRO_ACTIVE.equals(status);
    }

    public boolean isExpiringSoon() {
        return PRO_EXPIRING_SOON.equals(status);
    }

    public boolean isExpiredInGrace() {
        return PRO_EXPIRED_IN_GRACE.equals(status);
    }

    public boolean isExpired() {
        return PRO_EXPIRED.equals(status);
    }
}

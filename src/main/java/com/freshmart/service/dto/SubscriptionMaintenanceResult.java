package com.freshmart.service.dto;

import java.time.LocalDate;

public class SubscriptionMaintenanceResult {

    private final LocalDate runDate;
    private final int scannedUsers;
    private final int downgradedUsers;
    private final int createdNotifications;

    public SubscriptionMaintenanceResult(LocalDate runDate, int scannedUsers, int downgradedUsers, int createdNotifications) {
        this.runDate = runDate;
        this.scannedUsers = scannedUsers;
        this.downgradedUsers = downgradedUsers;
        this.createdNotifications = createdNotifications;
    }

    public LocalDate getRunDate() {
        return runDate;
    }

    public int getScannedUsers() {
        return scannedUsers;
    }

    public int getDowngradedUsers() {
        return downgradedUsers;
    }

    public int getCreatedNotifications() {
        return createdNotifications;
    }
}

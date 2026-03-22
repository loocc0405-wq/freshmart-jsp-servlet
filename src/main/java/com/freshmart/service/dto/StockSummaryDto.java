package com.freshmart.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockSummaryDto {
    private final int totalIn;
    private final int totalRemaining;
    private final int totalReserved;
    private final int availableQty;
    private final int expiredQty;
    private final int consumedQty;
    private final int activeLotsCount;
    private final int expiredLotsCount;
    private final int expiringQty;
    private final BigDecimal availableValue;
    private final LocalDate nearestExpiry;

    public StockSummaryDto(int totalIn,
            int totalRemaining,
            int totalReserved,
            int availableQty,
            int expiredQty,
            int consumedQty,
            int activeLotsCount,
            int expiredLotsCount,
            int expiringQty,
            BigDecimal availableValue,
            LocalDate nearestExpiry) {
        this.totalIn = totalIn;
        this.totalRemaining = totalRemaining;
        this.totalReserved = totalReserved;
        this.availableQty = availableQty;
        this.expiredQty = expiredQty;
        this.consumedQty = consumedQty;
        this.activeLotsCount = activeLotsCount;
        this.expiredLotsCount = expiredLotsCount;
        this.expiringQty = expiringQty;
        this.availableValue = availableValue;
        this.nearestExpiry = nearestExpiry;
    }

    public int getTotalIn() {
        return totalIn;
    }

    public int getTotalRemaining() {
        return totalRemaining;
    }

    public int getTotalReserved() {
        return totalReserved;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    public int getExpiredQty() {
        return expiredQty;
    }

    public int getConsumedQty() {
        return consumedQty;
    }

    public int getActiveLotsCount() {
        return activeLotsCount;
    }

    public int getExpiredLotsCount() {
        return expiredLotsCount;
    }

    public int getExpiringQty() {
        return expiringQty;
    }

    public BigDecimal getAvailableValue() {
        return availableValue;
    }

    public LocalDate getNearestExpiry() {
        return nearestExpiry;
    }
}

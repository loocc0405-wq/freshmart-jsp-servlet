package com.freshmart.service.dto;

import java.time.LocalDate;

public class FefoAllocationLot {
    private final Long lotId;
    private final LocalDate importDate;
    private final LocalDate expiryDate;
    private final int qtyLeftBefore;
    private final int allocatedQty;
    private final long daysUntilExpiry;
    private final boolean nearExpiry;

    public FefoAllocationLot(Long lotId,
                             LocalDate importDate,
                             LocalDate expiryDate,
                             int qtyLeftBefore,
                             int allocatedQty,
                             long daysUntilExpiry,
                             boolean nearExpiry) {
        this.lotId = lotId;
        this.importDate = importDate;
        this.expiryDate = expiryDate;
        this.qtyLeftBefore = qtyLeftBefore;
        this.allocatedQty = allocatedQty;
        this.daysUntilExpiry = daysUntilExpiry;
        this.nearExpiry = nearExpiry;
    }

    public Long getLotId() { return lotId; }
    public LocalDate getImportDate() { return importDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getQtyLeftBefore() { return qtyLeftBefore; }
    public int getAllocatedQty() { return allocatedQty; }
    public long getDaysUntilExpiry() { return daysUntilExpiry; }
    public boolean isNearExpiry() { return nearExpiry; }
}

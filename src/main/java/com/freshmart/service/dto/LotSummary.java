package com.freshmart.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing lot status for UI display.
 */
public class LotSummary {
    private Long id;
    private Long productId;
    private String productName;
    private String supplierName;
    private LocalDate importDate;
    private LocalDate expiryDate;
    private int qtyIn;
    private int qtyLeft;
    private int qtyConsumed;
    private BigDecimal importPrice;
    private String status; // AVAILABLE, EXPIRED, CONSUMED

    public LotSummary(Long id, Long productId, String productName, String supplierName,
                      LocalDate importDate, LocalDate expiryDate, int qtyIn, int qtyLeft,
                      BigDecimal importPrice, String status) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.supplierName = supplierName;
        this.importDate = importDate;
        this.expiryDate = expiryDate;
        this.qtyIn = qtyIn;
        this.qtyLeft = qtyLeft;
        this.qtyConsumed = qtyIn - qtyLeft;
        this.importPrice = importPrice;
        this.status = status;
    }

    // Getters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getSupplierName() { return supplierName; }
    public LocalDate getImportDate() { return importDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getQtyIn() { return qtyIn; }
    public int getQtyLeft() { return qtyLeft; }
    public int getQtyConsumed() { return qtyConsumed; }
    public BigDecimal getImportPrice() { return importPrice; }
    public String getStatus() { return status; }

    /**
     * Calculate days until expiry (can be negative if already expired).
     */
    public long getDaysUntilExpiry(LocalDate today) {
        return expiryDate.toEpochDay() - today.toEpochDay();
    }

    /**
     * Check if lot is expiring soon (within N days).
     */
    public boolean isExpiringWithin(LocalDate today, int days) {
        long daysLeft = getDaysUntilExpiry(today);
        return daysLeft >= 0 && daysLeft <= days;
    }

    /**
     * Check if lot is already expired.
     */
    public boolean isExpired(LocalDate today) {
        return expiryDate.isBefore(today);
    }
}

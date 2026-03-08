package com.freshmart.service.dto;

import java.time.LocalDate;

public class InventoryLotFilter {
    private Long productId;
    private Long supplierId;
    private String status; // AVAILABLE, EXPIRING, EXPIRED, CONSUMED

    private LocalDate importFrom;
    private LocalDate importTo;

    private LocalDate expiryFrom;
    private LocalDate expiryTo;

    private Integer minQtyLeft;
    private Integer maxQtyLeft;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getImportFrom() {
        return importFrom;
    }

    public void setImportFrom(LocalDate importFrom) {
        this.importFrom = importFrom;
    }

    public LocalDate getImportTo() {
        return importTo;
    }

    public void setImportTo(LocalDate importTo) {
        this.importTo = importTo;
    }

    public LocalDate getExpiryFrom() {
        return expiryFrom;
    }

    public void setExpiryFrom(LocalDate expiryFrom) {
        this.expiryFrom = expiryFrom;
    }

    public LocalDate getExpiryTo() {
        return expiryTo;
    }

    public void setExpiryTo(LocalDate expiryTo) {
        this.expiryTo = expiryTo;
    }

    public Integer getMinQtyLeft() {
        return minQtyLeft;
    }

    public void setMinQtyLeft(Integer minQtyLeft) {
        this.minQtyLeft = minQtyLeft;
    }

    public Integer getMaxQtyLeft() {
        return maxQtyLeft;
    }

    public void setMaxQtyLeft(Integer maxQtyLeft) {
        this.maxQtyLeft = maxQtyLeft;
    }
}

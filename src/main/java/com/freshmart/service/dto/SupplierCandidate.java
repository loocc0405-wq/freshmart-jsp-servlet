package com.freshmart.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO chứa thông tin supplier candidate từ lịch sử nhập hàng (product_lots)
 */
public class SupplierCandidate {
    private Long supplierId;
    private String supplierName;
    private Integer supplierLeadTimeDays;
    private BigDecimal avgImportPrice;
    private LocalDate lastImportDate;
    private long lotCount;
    private long totalQtyIn;

    public SupplierCandidate() {}

    public SupplierCandidate(Long supplierId, String supplierName, Integer supplierLeadTimeDays,
                            BigDecimal avgImportPrice, LocalDate lastImportDate,
                            long lotCount, long totalQtyIn) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierLeadTimeDays = supplierLeadTimeDays;
        this.avgImportPrice = avgImportPrice;
        this.lastImportDate = lastImportDate;
        this.lotCount = lotCount;
        this.totalQtyIn = totalQtyIn;
    }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public Integer getSupplierLeadTimeDays() { return supplierLeadTimeDays; }
    public void setSupplierLeadTimeDays(Integer supplierLeadTimeDays) { this.supplierLeadTimeDays = supplierLeadTimeDays; }

    public BigDecimal getAvgImportPrice() { return avgImportPrice; }
    public void setAvgImportPrice(BigDecimal avgImportPrice) { this.avgImportPrice = avgImportPrice; }

    public LocalDate getLastImportDate() { return lastImportDate; }
    public void setLastImportDate(LocalDate lastImportDate) { this.lastImportDate = lastImportDate; }

    public long getLotCount() { return lotCount; }
    public void setLotCount(long lotCount) { this.lotCount = lotCount; }

    public long getTotalQtyIn() { return totalQtyIn; }
    public void setTotalQtyIn(long totalQtyIn) { this.totalQtyIn = totalQtyIn; }
}

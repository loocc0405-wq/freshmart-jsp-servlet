package com.freshmart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "revenue_daily")
public class RevenueDaily {

    @Id
    @Column(name = "revenue_date", nullable = false)
    private LocalDate revenueDate;

    @Column(name = "total_revenue", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    public RevenueDaily() {}

    public RevenueDaily(LocalDate revenueDate, BigDecimal totalRevenue) {
        this.revenueDate = revenueDate;
        this.totalRevenue = totalRevenue;
    }

    public LocalDate getRevenueDate() {
        return revenueDate;
    }

    public void setRevenueDate(LocalDate revenueDate) {
        this.revenueDate = revenueDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}

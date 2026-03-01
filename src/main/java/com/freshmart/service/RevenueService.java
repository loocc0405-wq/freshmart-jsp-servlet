package com.freshmart.service;

import com.freshmart.entity.RevenueDaily;
import com.freshmart.repository.RevenueDailyRepository;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueService {

    private final RevenueDailyRepository revenueRepo = new RevenueDailyRepository();

    /**
     * Upsert revenue_daily (revenue_date, total_revenue).
     * MUST be called inside an existing transaction.
     */
    public void addRevenue(EntityManager em, LocalDate date, BigDecimal amount) {
        if (amount == null) return;

        RevenueDaily rd = revenueRepo.findByDate(em, date)
                .orElseGet(() -> new RevenueDaily(date, BigDecimal.ZERO));

        rd.setTotalRevenue(rd.getTotalRevenue().add(amount));
        revenueRepo.save(em, rd);
    }
}

package com.freshmart.service.dto;

import com.freshmart.entity.Supplier;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing comprehensive supplier performance metrics and scorecard.
 */
public class SupplierScorecard {
    private final Supplier supplier;
    
    // Statistics
    private final long totalLots;
    private final long totalQtyIn;
    private final BigDecimal totalImportValue;
    private final long distinctProducts;
    private final Double avgLeadTimeDays;
    private final long nearExpiryLots;
    private final long expiredLots;
    private final double expiryRiskRate;
    private final LocalDate lastImportDate;
    
    // Scoring
    private final double score;
    private final String rating;

    public SupplierScorecard(Supplier supplier, long totalLots, long totalQtyIn, 
                            BigDecimal totalImportValue, long distinctProducts,
                            Double avgLeadTimeDays, long nearExpiryLots, long expiredLots,
                            LocalDate lastImportDate) {
        this.supplier = supplier;
        this.totalLots = totalLots;
        this.totalQtyIn = totalQtyIn;
        this.totalImportValue = totalImportValue != null ? totalImportValue : BigDecimal.ZERO;
        this.distinctProducts = distinctProducts;
        this.avgLeadTimeDays = avgLeadTimeDays != null ? avgLeadTimeDays : 
                              (supplier.getLeadTimeDays() != null ? supplier.getLeadTimeDays().doubleValue() : 1.0);
        this.nearExpiryLots = nearExpiryLots;
        this.expiredLots = expiredLots;
        this.expiryRiskRate = totalLots > 0 ? (nearExpiryLots + expiredLots) / (double) totalLots : 0.0;
        this.lastImportDate = lastImportDate;
        
        // Calculate score and rating
        this.score = calculateScore();
        this.rating = calculateRating();
    }

    private double calculateScore() {
        double s = 0.0;
        
        // Volume factor (0-30 points): more lots/products = better
        if (totalLots > 0) {
            s += Math.min(15, totalLots / 2.0);
        }
        if (distinctProducts > 0) {
            s += Math.min(15, distinctProducts * 3.0);
        }
        
        // Lead time factor (0-25 points): lower is better
        if (avgLeadTimeDays != null && avgLeadTimeDays > 0) {
            double leadScore = 25.0 - (avgLeadTimeDays * 2.0);
            s += Math.max(0, Math.min(25, leadScore));
        }
        
        // Expiry risk factor (0-30 points): lower risk is better
        double riskPenalty = expiryRiskRate * 30.0;
        s += Math.max(0, 30.0 - riskPenalty);
        
        // Recency factor (0-15 points): recent imports are better
        if (lastImportDate != null) {
            long daysSinceLastImport = java.time.temporal.ChronoUnit.DAYS.between(lastImportDate, LocalDate.now());
            if (daysSinceLastImport <= 7) {
                s += 15;
            } else if (daysSinceLastImport <= 30) {
                s += 10;
            } else if (daysSinceLastImport <= 90) {
                s += 5;
            }
        }
        
        return Math.min(100, Math.max(0, s));
    }

    private String calculateRating() {
        if (expiryRiskRate > 0.5) {
            return "Risky";
        }
        if (score >= 80) {
            return "Excellent";
        } else if (score >= 60) {
            return "Good";
        } else if (score >= 40) {
            return "Warning";
        } else {
            return "Risky";
        }
    }

    // Getters
    public Supplier getSupplier() { return supplier; }
    public long getTotalLots() { return totalLots; }
    public long getTotalQtyIn() { return totalQtyIn; }
    public BigDecimal getTotalImportValue() { return totalImportValue; }
    public long getDistinctProducts() { return distinctProducts; }
    public Double getAvgLeadTimeDays() { return avgLeadTimeDays; }
    public long getNearExpiryLots() { return nearExpiryLots; }
    public long getExpiredLots() { return expiredLots; }
    public double getExpiryRiskRate() { return expiryRiskRate; }
    public LocalDate getLastImportDate() { return lastImportDate; }
    public double getScore() { return score; }
    public String getRating() { return rating; }
}

package com.freshmart.util;

import com.freshmart.entity.ProductLot;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FEFO (First Expired, First Out) utility methods.
 * Provides helpers for sorting and selecting lots based on FEFO principle.
 */
public final class FEFOUtil {
    private FEFOUtil() {}

    /**
     * Sort lots in FEFO order (First Expired, First Out).
     * Lots expiring sooner come first.
     */
    public static List<ProductLot> sortByFEFO(List<ProductLot> lots) {
        return lots.stream()
                .sorted(Comparator
                        .comparing(ProductLot::getExpiryDate)    // Sooner expiry first
                        .thenComparing(ProductLot::getImportDate) // Then older import date
                        .thenComparing(ProductLot::getId))        // Then by ID
                .collect(Collectors.toList());
    }

    /**
     * Get available lots (not expired, have qty > 0) sorted by FEFO.
     */
    public static List<ProductLot> getAvailableLotsFEFO(List<ProductLot> lots, LocalDate today) {
        return sortByFEFO(lots.stream()
                .filter(l -> l.getQtyLeft() > 0)                    // Not consumed
                .filter(l -> !l.getExpiryDate().isBefore(today))    // Not expired
                .collect(Collectors.toList()));
    }

    /**
     * Get expired lots.
     */
    public static List<ProductLot> getExpiredLots(List<ProductLot> lots, LocalDate today) {
        return lots.stream()
                .filter(l -> l.getExpiryDate().isBefore(today))
                .sorted(Comparator.comparing(ProductLot::getExpiryDate))
                .collect(Collectors.toList());
    }

    /**
     * Get lots expiring within N days.
     */
    public static List<ProductLot> getLotsExpiringWithin(List<ProductLot> lots, LocalDate today, int days) {
        LocalDate deadline = today.plusDays(days);
        return lots.stream()
                .filter(l -> l.getQtyLeft() > 0)
                .filter(l -> !l.getExpiryDate().isBefore(today))
                .filter(l -> !l.getExpiryDate().isAfter(deadline))
                .sorted(Comparator.comparing(ProductLot::getExpiryDate))
                .collect(Collectors.toList());
    }

    /**
     * Calculate days until expiry for a lot.
     */
    public static long getDaysUntilExpiry(ProductLot lot, LocalDate today) {
        return lot.getExpiryDate().toEpochDay() - today.toEpochDay();
    }

    /**
     * Check if a lot should be prioritized for use (expiring soon risk).
     */
    public static boolean needsUrgentUse(ProductLot lot, LocalDate today, int urgentDays) {
        return getDaysUntilExpiry(lot, today) <= urgentDays && getDaysUntilExpiry(lot, today) >= 0;
    }

    /**
     * Get total consumable quantity from a list of lots.
     */
    public static int getTotalAvailableQty(List<ProductLot> lots, LocalDate today) {
        return getAvailableLotsFEFO(lots, today).stream()
                .mapToInt(ProductLot::getQtyLeft)
                .sum();
    }
}

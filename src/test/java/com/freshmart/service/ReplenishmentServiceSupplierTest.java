package com.freshmart.service;

import com.freshmart.service.dto.SupplierCandidate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test ranking supplier logic
 */
public class ReplenishmentServiceSupplierTest {

    @Test
    public void testRankSuppliers_ByLeadTime() {
        List<SupplierCandidate> candidates = new ArrayList<>();
        candidates.add(new SupplierCandidate(1L, "Supplier A", 5, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));
        candidates.add(new SupplierCandidate(2L, "Supplier B", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));
        candidates.add(new SupplierCandidate(3L, "Supplier C", 7, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));

        SupplierCandidate best = rankSuppliers(candidates);
        
        assertEquals(2L, best.getSupplierId());
        assertEquals("Supplier B", best.getSupplierName());
        assertEquals(3, best.getSupplierLeadTimeDays());
    }

    @Test
    public void testRankSuppliers_ByPrice() {
        List<SupplierCandidate> candidates = new ArrayList<>();
        candidates.add(new SupplierCandidate(1L, "Supplier A", 3, 
            new BigDecimal("120"), LocalDate.now(), 10, 1000));
        candidates.add(new SupplierCandidate(2L, "Supplier B", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));
        candidates.add(new SupplierCandidate(3L, "Supplier C", 3, 
            new BigDecimal("110"), LocalDate.now(), 10, 1000));

        SupplierCandidate best = rankSuppliers(candidates);
        
        assertEquals(2L, best.getSupplierId());
        assertEquals("Supplier B", best.getSupplierName());
        assertEquals(new BigDecimal("100"), best.getAvgImportPrice());
    }

    @Test
    public void testRankSuppliers_ByLastImportDate() {
        LocalDate today = LocalDate.now();
        List<SupplierCandidate> candidates = new ArrayList<>();
        candidates.add(new SupplierCandidate(1L, "Supplier A", 3, 
            new BigDecimal("100"), today.minusDays(10), 10, 1000));
        candidates.add(new SupplierCandidate(2L, "Supplier B", 3, 
            new BigDecimal("100"), today.minusDays(2), 10, 1000));
        candidates.add(new SupplierCandidate(3L, "Supplier C", 3, 
            new BigDecimal("100"), today.minusDays(5), 10, 1000));

        SupplierCandidate best = rankSuppliers(candidates);
        
        assertEquals(2L, best.getSupplierId());
        assertEquals(today.minusDays(2), best.getLastImportDate());
    }

    @Test
    public void testRankSuppliers_ByLotCount() {
        List<SupplierCandidate> candidates = new ArrayList<>();
        candidates.add(new SupplierCandidate(1L, "Supplier A", 3, 
            new BigDecimal("100"), LocalDate.now(), 5, 1000));
        candidates.add(new SupplierCandidate(2L, "Supplier B", 3, 
            new BigDecimal("100"), LocalDate.now(), 15, 1000));
        candidates.add(new SupplierCandidate(3L, "Supplier C", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));

        SupplierCandidate best = rankSuppliers(candidates);
        
        assertEquals(2L, best.getSupplierId());
        assertEquals(15, best.getLotCount());
    }

    @Test
    public void testRankSuppliers_ByTotalQtyIn() {
        List<SupplierCandidate> candidates = new ArrayList<>();
        candidates.add(new SupplierCandidate(1L, "Supplier A", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 500));
        candidates.add(new SupplierCandidate(2L, "Supplier B", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 2000));
        candidates.add(new SupplierCandidate(3L, "Supplier C", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));

        SupplierCandidate best = rankSuppliers(candidates);
        
        assertEquals(2L, best.getSupplierId());
        assertEquals(2000, best.getTotalQtyIn());
    }

    @Test
    public void testRankSuppliers_BySupplierId() {
        List<SupplierCandidate> candidates = new ArrayList<>();
        candidates.add(new SupplierCandidate(5L, "Supplier E", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));
        candidates.add(new SupplierCandidate(2L, "Supplier B", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));
        candidates.add(new SupplierCandidate(8L, "Supplier H", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));

        SupplierCandidate best = rankSuppliers(candidates);
        
        assertEquals(2L, best.getSupplierId());
    }

    @Test
    public void testRankSuppliers_EmptyList() {
        List<SupplierCandidate> candidates = new ArrayList<>();
        SupplierCandidate best = rankSuppliers(candidates);
        assertNull(best);
    }

    @Test
    public void testRankSuppliers_NullValues() {
        List<SupplierCandidate> candidates = new ArrayList<>();
        candidates.add(new SupplierCandidate(1L, "Supplier A", null, 
            null, null, 10, 1000));
        candidates.add(new SupplierCandidate(2L, "Supplier B", 3, 
            new BigDecimal("100"), LocalDate.now(), 10, 1000));

        SupplierCandidate best = rankSuppliers(candidates);
        
        assertEquals(2L, best.getSupplierId());
    }

    // Helper method - copy logic từ ReplenishmentService
    private SupplierCandidate rankSuppliers(List<SupplierCandidate> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort(Comparator
                .comparing(SupplierCandidate::getSupplierLeadTimeDays, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SupplierCandidate::getAvgImportPrice, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SupplierCandidate::getLastImportDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(SupplierCandidate::getLotCount, Comparator.reverseOrder())
                .thenComparing(SupplierCandidate::getTotalQtyIn, Comparator.reverseOrder())
                .thenComparing(SupplierCandidate::getSupplierId)
        );

        return candidates.get(0);
    }
}

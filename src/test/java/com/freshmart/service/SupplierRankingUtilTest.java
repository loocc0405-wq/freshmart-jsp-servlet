package com.freshmart.service;

import com.freshmart.service.dto.SupplierCandidate;
import com.freshmart.service.util.SupplierRankingUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho SupplierRankingUtil.pickBest().
 * Kiểm tra thứ tự ưu tiên ranking và các edge case null-safe.
 */
class SupplierRankingUtilTest {

    @Test
    void pickBest_nullList_returnsNull() {
        assertNull(SupplierRankingUtil.pickBest(null));
    }

    @Test
    void pickBest_emptyList_returnsNull() {
        assertNull(SupplierRankingUtil.pickBest(new ArrayList<>()));
    }

    @Test
    void pickBest_singleCandidate_returnsThat() {
        SupplierCandidate c = candidate(1L, 3, new BigDecimal("100"), LocalDate.now(), 5, 100);
        List<SupplierCandidate> list = new ArrayList<>(List.of(c));
        assertEquals(1L, SupplierRankingUtil.pickBest(list).getSupplierId());
    }

    @Test
    void pickBest_prefersShorterLeadTime() {
        SupplierCandidate fast = candidate(1L, 1, new BigDecimal("200"), LocalDate.now(), 3, 50);
        SupplierCandidate slow = candidate(2L, 5, new BigDecimal("100"), LocalDate.now(), 10, 200);
        List<SupplierCandidate> list = new ArrayList<>(List.of(slow, fast));
        assertEquals(1L, SupplierRankingUtil.pickBest(list).getSupplierId());
    }

    @Test
    void pickBest_sameLeadTime_prefersLowerPrice() {
        SupplierCandidate cheap = candidate(1L, 3, new BigDecimal("80"), LocalDate.now(), 5, 100);
        SupplierCandidate expensive = candidate(2L, 3, new BigDecimal("150"), LocalDate.now(), 5, 100);
        List<SupplierCandidate> list = new ArrayList<>(List.of(expensive, cheap));
        assertEquals(1L, SupplierRankingUtil.pickBest(list).getSupplierId());
    }

    @Test
    void pickBest_sameLeadTimeAndPrice_prefersMoreRecentImport() {
        LocalDate recent = LocalDate.now();
        LocalDate old = LocalDate.now().minusDays(30);
        SupplierCandidate newer = candidate(1L, 3, new BigDecimal("100"), recent, 5, 100);
        SupplierCandidate older = candidate(2L, 3, new BigDecimal("100"), old, 5, 100);
        List<SupplierCandidate> list = new ArrayList<>(List.of(older, newer));
        assertEquals(1L, SupplierRankingUtil.pickBest(list).getSupplierId());
    }

    @Test
    void pickBest_nullLeadTime_goesToEnd() {
        SupplierCandidate withLead = candidate(1L, 2, new BigDecimal("100"), LocalDate.now(), 5, 100);
        SupplierCandidate noLead = candidateNullLead(2L, new BigDecimal("50"), LocalDate.now(), 10, 200);
        List<SupplierCandidate> list = new ArrayList<>(List.of(noLead, withLead));
        // supplier with leadTime should win over null leadTime
        assertEquals(1L, SupplierRankingUtil.pickBest(list).getSupplierId());
    }

    @Test
    void pickBest_tiebreaker_smallerIdWins() {
        SupplierCandidate s1 = candidate(1L, 3, new BigDecimal("100"), LocalDate.now(), 5, 100);
        SupplierCandidate s2 = candidate(2L, 3, new BigDecimal("100"), LocalDate.now(), 5, 100);
        List<SupplierCandidate> list = new ArrayList<>(List.of(s2, s1));
        assertEquals(1L, SupplierRankingUtil.pickBest(list).getSupplierId());
    }

    // --- helpers ---

    private SupplierCandidate candidate(Long id, int leadTime, BigDecimal avgPrice,
                                        LocalDate lastImport, long lotCount, long totalQty) {
        return new SupplierCandidate(id, "Supplier-" + id, leadTime, avgPrice, lastImport, lotCount, totalQty);
    }

    private SupplierCandidate candidateNullLead(Long id, BigDecimal avgPrice,
                                                LocalDate lastImport, long lotCount, long totalQty) {
        return new SupplierCandidate(id, "Supplier-" + id, null, avgPrice, lastImport, lotCount, totalQty);
    }
}

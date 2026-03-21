package com.freshmart.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test cho pagination clamp logic và certificate filter.
 * Không cần DB - test pure logic tính toán.
 */
class SupplierPaginationTest {

    // ---- Pagination clamp helpers (mirror logic trong servlet) ----

    static int clampPage(int page, int totalItems, int pageSize) {
        int totalPages = totalItems == 0 ? 1 : (int) ((totalItems + pageSize - 1) / pageSize);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        return page;
    }

    static int calcStartIdx(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    static <T> List<T> paginate(List<T> list, int page, int pageSize) {
        int totalPages = list.isEmpty() ? 1 : (int) ((list.size() + pageSize - 1) / pageSize);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, list.size());
        return start < list.size() ? list.subList(start, end) : List.of();
    }

    // ---- Pagination tests ----

    @Test
    void pagination_normalPage_returnsCorrectSlice() {
        List<Integer> items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        List<Integer> page1 = paginate(new ArrayList<>(items), 1, 10);
        assertEquals(10, page1.size());
        assertEquals(1, page1.get(0));
    }

    @Test
    void pagination_lastPage_returnsRemainder() {
        List<Integer> items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        List<Integer> page2 = paginate(new ArrayList<>(items), 2, 10);
        assertEquals(2, page2.size());
        assertEquals(11, page2.get(0));
    }

    @Test
    void pagination_pageOverflow_clampsToLastPage() {
        List<Integer> items = List.of(1, 2, 3, 4, 5);
        // page=5 but only 1 page exists → should clamp to page 1
        List<Integer> result = paginate(new ArrayList<>(items), 5, 10);
        assertEquals(5, result.size(), "Should return all items on clamped page");
    }

    @Test
    void pagination_emptyList_returnsEmpty() {
        List<Integer> result = paginate(new ArrayList<>(), 1, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void pagination_emptyList_pageOverflow_doesNotThrow() {
        // This was the bug: page=3, empty list → startIdx=20, subList throws
        assertDoesNotThrow(() -> paginate(new ArrayList<>(), 3, 10));
    }

    @Test
    void pagination_pageZero_clampsToOne() {
        List<Integer> items = List.of(1, 2, 3);
        List<Integer> result = paginate(new ArrayList<>(items), 0, 10);
        assertEquals(3, result.size());
    }

    @Test
    void pagination_negativePage_clampsToOne() {
        List<Integer> items = List.of(1, 2, 3);
        List<Integer> result = paginate(new ArrayList<>(items), -5, 10);
        assertEquals(3, result.size());
    }

    @Test
    void clampPage_afterFilterReducesItems_returnsValidPage() {
        // Scenario: user was on page 3 (30 items), filter reduces to 5 items
        int clamped = clampPage(3, 5, 10);
        assertEquals(1, clamped, "Page 3 should clamp to page 1 when only 5 items remain");
    }

    @Test
    void clampPage_exactlyOnLastPage_staysOnLastPage() {
        int clamped = clampPage(2, 20, 10);
        assertEquals(2, clamped);
    }

    // ---- Certificate filter tests ----

    static boolean certMatches(String supplierCert, String filterValue) {
        if (filterValue == null || filterValue.isBlank()) return true;
        if (supplierCert == null) return false;
        return supplierCert.toLowerCase().contains(filterValue.toLowerCase());
    }

    @Test
    void certFilter_null_matchesAll() {
        assertTrue(certMatches("ISO9001", null));
        assertTrue(certMatches(null, null));
    }

    @Test
    void certFilter_exactMatch_matches() {
        assertTrue(certMatches("ISO9001", "ISO9001"));
    }

    @Test
    void certFilter_partialMatch_matches() {
        assertTrue(certMatches("ISO9001:2015", "ISO9001"));
    }

    @Test
    void certFilter_caseInsensitive_matches() {
        assertTrue(certMatches("VietGAP", "vietgap"));
    }

    @Test
    void certFilter_noMatch_returnsFalse() {
        assertFalse(certMatches("ISO9001", "VietGAP"));
    }

    @Test
    void certFilter_supplierNullCert_returnsFalse() {
        assertFalse(certMatches(null, "ISO9001"));
    }
}

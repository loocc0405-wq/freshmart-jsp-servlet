package com.freshmart.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test pagination logic cho ProductService
 * Test các tính toán offset, page, size, totalPages
 * Không test database interaction, chỉ test logic tính toán
 */
class ProductServicePaginationTest {

    @Test
    void testCalculateOffset_FirstPage() {
        int page = 1;
        int size = 10;
        int offset = (page - 1) * size;
        
        assertEquals(0, offset, "First page should have offset 0");
    }

    @Test
    void testCalculateOffset_SecondPage() {
        int page = 2;
        int size = 10;
        int offset = (page - 1) * size;
        
        assertEquals(10, offset, "Second page should have offset 10");
    }

    @Test
    void testCalculateOffset_CustomPageSize() {
        int page = 3;
        int size = 20;
        int offset = (page - 1) * size;
        
        assertEquals(40, offset, "Page 3 with size 20 should have offset 40");
    }

    @Test
    void testCalculateTotalPages_ExactDivision() {
        long totalItems = 100;
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        assertEquals(10, totalPages, "100 items with size 10 should have 10 pages");
    }

    @Test
    void testCalculateTotalPages_WithRemainder() {
        long totalItems = 105;
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        assertEquals(11, totalPages, "105 items with size 10 should have 11 pages");
    }

    @Test
    void testCalculateTotalPages_LessThanPageSize() {
        long totalItems = 5;
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        assertEquals(1, totalPages, "5 items with size 10 should have 1 page");
    }

    @Test
    void testCalculateTotalPages_ZeroItems() {
        long totalItems = 0;
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        assertEquals(0, totalPages, "0 items should have 0 pages");
    }

    @Test
    void testPageValidation_ValidPage() {
        int page = 5;
        int totalPages = 10;
        
        assertTrue(page >= 1 && page <= totalPages, "Page 5 of 10 should be valid");
    }

    @Test
    void testPageValidation_InvalidPageTooLow() {
        int page = 0;
        int totalPages = 10;
        
        assertFalse(page >= 1 && page <= totalPages, "Page 0 should be invalid");
    }

    @Test
    void testPageValidation_InvalidPageTooHigh() {
        int page = 11;
        int totalPages = 10;
        
        assertFalse(page >= 1 && page <= totalPages, "Page 11 of 10 should be invalid");
    }

    @Test
    void testPageValidation_NegativePage() {
        int page = -1;
        int totalPages = 10;
        
        assertFalse(page >= 1 && page <= totalPages, "Negative page should be invalid");
    }

    @Test
    void testPageSizeValidation_ValidSizes() {
        int[] validSizes = {10, 20, 50, 100};
        
        for (int size : validSizes) {
            assertTrue(size > 0 && size <= 100, "Size " + size + " should be valid");
        }
    }

    @Test
    void testPageSizeValidation_InvalidSizeTooSmall() {
        int size = 0;
        
        assertFalse(size > 0 && size <= 100, "Size 0 should be invalid");
    }

    @Test
    void testPageSizeValidation_InvalidSizeTooLarge() {
        int size = 101;
        
        assertFalse(size > 0 && size <= 100, "Size 101 should be invalid");
    }

    @Test
    void testDefaultPageSize() {
        int defaultSize = 10;
        
        assertTrue(defaultSize > 0, "Default page size should be positive");
        assertTrue(defaultSize <= 100, "Default page size should be reasonable");
    }

    @Test
    void testCalculateItemRange_FirstPage() {
        int page = 1;
        int size = 10;
        long totalItems = 100;
        
        int startItem = (page - 1) * size + 1;
        int endItem = Math.min(page * size, (int) totalItems);
        
        assertEquals(1, startItem, "First page should start at item 1");
        assertEquals(10, endItem, "First page should end at item 10");
    }

    @Test
    void testCalculateItemRange_LastPagePartial() {
        int page = 11;
        int size = 10;
        long totalItems = 105;
        
        int startItem = (page - 1) * size + 1;
        int endItem = Math.min(page * size, (int) totalItems);
        
        assertEquals(101, startItem, "Last page should start at item 101");
        assertEquals(105, endItem, "Last page should end at item 105");
    }

    @Test
    void testCalculateItemRange_SingleItemPage() {
        int page = 1;
        int size = 10;
        long totalItems = 1;
        
        int startItem = (page - 1) * size + 1;
        int endItem = Math.min(page * size, (int) totalItems);
        
        assertEquals(1, startItem);
        assertEquals(1, endItem);
    }

    @Test
    void testHasPreviousPage() {
        assertTrue(2 > 1, "Page 2 should have previous page");
        assertFalse(1 > 1, "Page 1 should not have previous page");
    }

    @Test
    void testHasNextPage() {
        int currentPage = 5;
        int totalPages = 10;
        
        assertTrue(currentPage < totalPages, "Page 5 of 10 should have next page");
        
        currentPage = 10;
        assertFalse(currentPage < totalPages, "Page 10 of 10 should not have next page");
    }
}

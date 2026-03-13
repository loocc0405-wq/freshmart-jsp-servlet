package com.freshmart.service;

import com.freshmart.entity.Supplier;
import com.freshmart.service.dto.SupplierScorecard;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SupplierScorecardTest {

    @Test
    void testScorecardWithNoLots() {
        Supplier supplier = new Supplier("Test Supplier");
        supplier.setLeadTimeDays(3);
        
        SupplierScorecard scorecard = new SupplierScorecard(
            supplier, 0, 0, BigDecimal.ZERO, 0, null, 0, 0, null
        );
        
        assertEquals(0, scorecard.getTotalLots());
        assertEquals(0, scorecard.getTotalQtyIn());
        assertEquals(BigDecimal.ZERO, scorecard.getTotalImportValue());
        assertEquals(0, scorecard.getDistinctProducts());
        assertEquals(0, scorecard.getNearExpiryLots());
        assertEquals(0, scorecard.getExpiredLots());
        assertEquals(0.0, scorecard.getExpiryRiskRate(), 0.001);
        assertNull(scorecard.getLastImportDate());
        
        // Score should be low but not zero (has lead time factor)
        assertTrue(scorecard.getScore() >= 0);
        assertTrue(scorecard.getScore() <= 100);
    }

    @Test
    void testScorecardWithGoodPerformance() {
        Supplier supplier = new Supplier("Good Supplier");
        supplier.setLeadTimeDays(2);
        
        SupplierScorecard scorecard = new SupplierScorecard(
            supplier, 
            20,  // totalLots
            500, // totalQtyIn
            new BigDecimal("10000.00"), // totalImportValue
            10,  // distinctProducts
            2.0, // avgLeadTimeDays
            1,   // nearExpiryLots
            0,   // expiredLots
            LocalDate.now().minusDays(3) // lastImportDate - recent
        );
        
        assertEquals(20, scorecard.getTotalLots());
        assertEquals(500, scorecard.getTotalQtyIn());
        assertEquals(new BigDecimal("10000.00"), scorecard.getTotalImportValue());
        assertEquals(10, scorecard.getDistinctProducts());
        assertEquals(1, scorecard.getNearExpiryLots());
        assertEquals(0, scorecard.getExpiredLots());
        assertEquals(0.05, scorecard.getExpiryRiskRate(), 0.001); // 1/20 = 5%
        
        // Should have high score
        assertTrue(scorecard.getScore() >= 70, "Score should be high for good performance");
        assertEquals("Excellent", scorecard.getRating());
    }

    @Test
    void testScorecardWithHighExpiryRisk() {
        Supplier supplier = new Supplier("Risky Supplier");
        supplier.setLeadTimeDays(5);
        
        SupplierScorecard scorecard = new SupplierScorecard(
            supplier,
            10,  // totalLots
            100, // totalQtyIn
            new BigDecimal("2000.00"),
            3,   // distinctProducts
            5.0, // avgLeadTimeDays
            4,   // nearExpiryLots
            3,   // expiredLots
            LocalDate.now().minusDays(60) // lastImportDate - old
        );
        
        assertEquals(10, scorecard.getTotalLots());
        assertEquals(0.7, scorecard.getExpiryRiskRate(), 0.001); // 7/10 = 70%
        
        // Should have low score due to high risk
        assertTrue(scorecard.getScore() < 60, "Score should be low for high risk");
        assertEquals("Risky", scorecard.getRating());
    }

    @Test
    void testExpiryRiskRateCalculation() {
        Supplier supplier = new Supplier("Test");
        
        // 50% risk
        SupplierScorecard sc1 = new SupplierScorecard(
            supplier, 10, 100, BigDecimal.ZERO, 5, null, 3, 2, null
        );
        assertEquals(0.5, sc1.getExpiryRiskRate(), 0.001);
        
        // 0% risk
        SupplierScorecard sc2 = new SupplierScorecard(
            supplier, 10, 100, BigDecimal.ZERO, 5, null, 0, 0, null
        );
        assertEquals(0.0, sc2.getExpiryRiskRate(), 0.001);
        
        // 100% risk
        SupplierScorecard sc3 = new SupplierScorecard(
            supplier, 10, 100, BigDecimal.ZERO, 5, null, 5, 5, null
        );
        assertEquals(1.0, sc3.getExpiryRiskRate(), 0.001);
    }

    @Test
    void testRatingBasedOnScore() {
        Supplier supplier = new Supplier("Test");
        
        // Excellent rating (score >= 80)
        SupplierScorecard excellent = new SupplierScorecard(
            supplier, 30, 1000, new BigDecimal("20000"), 15, 1.0, 0, 0, LocalDate.now()
        );
        assertTrue(excellent.getScore() >= 80);
        assertEquals("Excellent", excellent.getRating());
        
        // Risky rating (high expiry risk overrides score)
        SupplierScorecard risky = new SupplierScorecard(
            supplier, 10, 100, new BigDecimal("1000"), 5, 3.0, 6, 0, LocalDate.now()
        );
        assertTrue(risky.getExpiryRiskRate() > 0.5);
        assertEquals("Risky", risky.getRating());
    }

    @Test
    void testScoreFactors() {
        Supplier supplier = new Supplier("Test");
        supplier.setLeadTimeDays(3);
        
        // Test volume factor
        SupplierScorecard highVolume = new SupplierScorecard(
            supplier, 50, 2000, new BigDecimal("50000"), 20, 3.0, 0, 0, LocalDate.now()
        );
        
        SupplierScorecard lowVolume = new SupplierScorecard(
            supplier, 5, 100, new BigDecimal("1000"), 2, 3.0, 0, 0, LocalDate.now()
        );
        
        assertTrue(highVolume.getScore() > lowVolume.getScore(), 
                  "Higher volume should result in higher score");
        
        // Test lead time factor
        SupplierScorecard fastLeadTime = new SupplierScorecard(
            supplier, 10, 200, new BigDecimal("5000"), 5, 1.0, 0, 0, LocalDate.now()
        );
        
        SupplierScorecard slowLeadTime = new SupplierScorecard(
            supplier, 10, 200, new BigDecimal("5000"), 5, 10.0, 0, 0, LocalDate.now()
        );
        
        assertTrue(fastLeadTime.getScore() > slowLeadTime.getScore(),
                  "Faster lead time should result in higher score");
    }

    @Test
    void testRecencyFactor() {
        Supplier supplier = new Supplier("Test");
        
        // Recent import (within 7 days)
        SupplierScorecard recent = new SupplierScorecard(
            supplier, 10, 200, new BigDecimal("5000"), 5, 3.0, 0, 0, LocalDate.now().minusDays(3)
        );
        
        // Old import (90+ days)
        SupplierScorecard old = new SupplierScorecard(
            supplier, 10, 200, new BigDecimal("5000"), 5, 3.0, 0, 0, LocalDate.now().minusDays(100)
        );
        
        assertTrue(recent.getScore() > old.getScore(),
                  "Recent imports should result in higher score");
    }

    @Test
    void testAvgLeadTimeFallback() {
        Supplier supplier = new Supplier("Test");
        supplier.setLeadTimeDays(5);
        
        // When avgLeadTimeDays is null, should use supplier's leadTimeDays
        SupplierScorecard scorecard = new SupplierScorecard(
            supplier, 10, 200, BigDecimal.ZERO, 5, null, 0, 0, null
        );
        
        assertEquals(5.0, scorecard.getAvgLeadTimeDays(), 0.001);
    }
}

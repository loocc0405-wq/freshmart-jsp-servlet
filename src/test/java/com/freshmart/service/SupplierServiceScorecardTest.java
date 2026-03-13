package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.Supplier;
import com.freshmart.service.dto.SupplierScorecard;
import com.freshmart.util.JpaExecutor;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SupplierServiceScorecardTest {

    private static SupplierService supplierService;
    private static ProductService productService;
    private static JpaExecutor executor;
    
    private static Long supplierId1;
    private static Long supplierId2;
    private static Long productId1;
    private static Long productId2;

    @BeforeAll
    static void setup() {
        supplierService = new SupplierService();
        productService = new ProductService();
        executor = new JpaExecutor();
        
        // Create test suppliers
        Supplier s1 = new Supplier("Excellent Supplier");
        s1.setEmail("excellent@test.com");
        s1.setPhone("1234567890");
        s1.setLeadTimeDays(2);
        s1 = supplierService.save(s1);
        supplierId1 = s1.getId();
        
        Supplier s2 = new Supplier("Risky Supplier");
        s2.setEmail("risky@test.com");
        s2.setPhone("0987654321");
        s2.setLeadTimeDays(7);
        s2 = supplierService.save(s2);
        supplierId2 = s2.getId();
        
        // Create test products
        Product p1 = new Product();
        p1.setName("Test Product 1");
        p1.setSellPrice(new BigDecimal("10.00"));
        p1 = productService.save(p1);
        productId1 = p1.getId();
        
        Product p2 = new Product();
        p2.setName("Test Product 2");
        p2.setSellPrice(new BigDecimal("20.00"));
        p2 = productService.save(p2);
        productId2 = p2.getId();
    }

    @Test
    @Order(1)
    void testScorecardWithNoLots() {
        List<SupplierScorecard> scorecards = supplierService.getSupplierScorecards(null);
        
        assertNotNull(scorecards);
        assertTrue(scorecards.size() >= 2);
        
        // Find our test suppliers
        SupplierScorecard sc1 = scorecards.stream()
            .filter(sc -> sc.getSupplier().getId().equals(supplierId1))
            .findFirst()
            
.orElse(null);
        
        assertNotNull(sc1);
        assertEquals(0, sc1.getTotalLots());
        assertEquals(0, sc1.getTotalQtyIn());
        assertEquals(BigDecimal.ZERO, sc1.getTotalImportValue());
    }

    @Test
    @Order(2)
    void testScorecardAfterAddingLots() {
        // Add lots for supplier 1 (good performance)
        executor.executeVoid(em -> {
            Supplier s1 = em.find(Supplier.class, supplierId1);
            Product p1 = em.find(Product.class, productId1);
            Product p2 = em.find(Product.class, productId2);
            
            // Add 5 lots with good expiry dates
            for (int i = 0; i < 5; i++) {
                ProductLot lot = new ProductLot();
                lot.setSupplier(s1);
                lot.setProduct(i < 3 ? p1 : p2);
                lot.setImportDate(LocalDate.now().minusDays(5));
                lot.setExpiryDate(LocalDate.now().plusDays(30 + i * 10));
                lot.setQtyIn(100);
                lot.setQtyLeft(100);
                lot.setImportPrice(new BigDecimal("5.00"));
                em.persist(lot);
            }
        });
        
        // Add lots for supplier 2 (risky performance)
        executor.executeVoid(em -> {
            Supplier s2 = em.find(Supplier.class, supplierId2);
            Product p1 = em.find(Product.class, productId1);
            
            // Add 4 lots with bad expiry dates
            ProductLot lot1 = new ProductLot();
            lot1.setSupplier(s2);
            lot1.setProduct(p1);
            lot1.setImportDate(LocalDate.now().minusDays(100));
            lot1.setExpiryDate(LocalDate.now().minusDays(10)); // expired
            lot1.setQtyIn(50);
            lot1.setQtyLeft(50);
            lot1.setImportPrice(new BigDecimal("3.00"));
            em.persist(lot1);
            
            ProductLot lot2 = new ProductLot();
            lot2.setSupplier(s2);
            lot2.setProduct(p1);
            lot2.setImportDate(LocalDate.now().minusDays(90));
            lot2.setExpiryDate(LocalDate.now().plusDays(3)); // near expiry
            lot2.setQtyIn(50);
            lot2.setQtyLeft(50);
            lot2.setImportPrice(new BigDecimal("3.00"));
            em.persist(lot2);
        });
        
        List<SupplierScorecard> scorecards = supplierService.getSupplierScorecards(null);
        
        SupplierScorecard sc1 = scorecards.stream()
            .filter(sc -> sc.getSupplier().getId().equals(supplierId1))
            .findFirst()
            .orElse(null);
        
        SupplierScorecard sc2 = scorecards.stream()
            .filter(sc -> sc.getSupplier().getId().equals(supplierId2))
            .findFirst()
            .orElse(null);
        
        assertNotNull(sc1);
        assertNotNull(sc2);
        
        // Verify supplier 1 stats
        assertEquals(5, sc1.getTotalLots());
        assertEquals(500, sc1.getTotalQtyIn());
        assertEquals(new BigDecimal("2500.00"), sc1.getTotalImportValue());
        assertEquals(2, sc1.getDistinctProducts());
        assertEquals(0, sc1.getNearExpiryLots());
        assertEquals(0, sc1.getExpiredLots());
        assertEquals(0.0, sc1.getExpiryRiskRate(), 0.001);
        assertNotNull(sc1.getLastImportDate());
        
        // Verify supplier 2 stats
        assertEquals(2, sc2.getTotalLots());
        assertEquals(100, sc2.getTotalQtyIn());
        assertEquals(new BigDecimal("300.00"), sc2.getTotalImportValue());
        assertEquals(1, sc2.getDistinctProducts());
        assertEquals(1, sc2.getNearExpiryLots());
        assertEquals(1, sc2.getExpiredLots());
        assertEquals(1.0, sc2.getExpiryRiskRate(), 0.001); // 100% risk
        
        // Verify scores and ratings
        assertTrue(sc1.getScore() > sc2.getScore(), 
                  "Supplier 1 should have higher score than Supplier 2");
        assertTrue(sc1.getScore() >= 60, "Supplier 1 should have good or excellent rating");
        assertTrue(List.of("Excellent", "Good").contains(sc1.getRating()), 
                  "Supplier 1 should have Excellent or Good rating");
        assertEquals("Risky", sc2.getRating());
    }

    @Test
    @Order(3)
    void testScorecardSortByScore() {
        List<SupplierScorecard> scorecards = supplierService.getSupplierScorecards("score");
        
        assertNotNull(scorecards);
        assertTrue(scorecards.size() >= 2);
        
        // Verify sorted by score descending
        for (int i = 0; i < scorecards.size() - 1; i++) {
            assertTrue(scorecards.get(i).getScore() >= scorecards.get(i + 1).getScore(),
                      "Scorecards should be sorted by score descending");
        }
    }

    @Test
    @Order(4)
    void testScorecardSortByExpiryRisk() {
        List<SupplierScorecard> scorecards = supplierService.getSupplierScorecards("expiryRisk");
        
        assertNotNull(scorecards);
        assertTrue(scorecards.size() >= 2);
        
        // Verify sorted by expiry risk descending
        for (int i = 0; i < scorecards.size() - 1; i++) {
            assertTrue(scorecards.get(i).getExpiryRiskRate() >= scorecards.get(i + 1).getExpiryRiskRate(),
                      "Scorecards should be sorted by expiry risk descending");
        }
    }

    @Test
    @Order(5)
    void testScorecardSortByImportValue() {
        List<SupplierScorecard> scorecards = supplierService.getSupplierScorecards("importValue");
        
        assertNotNull(scorecards);
        assertTrue(scorecards.size() >= 2);
        
        // Verify sorted by import value descending
        for (int i = 0; i < scorecards.size() - 1; i++) {
            assertTrue(scorecards.get(i).getTotalImportValue()
                      .compareTo(scorecards.get(i + 1).getTotalImportValue()) >= 0,
                      "Scorecards should be sorted by import value descending");
        }
    }

    @AfterAll
    static void cleanup() {
        // Clean up test data
        executor.executeVoid(em -> {
            // Delete lots first (foreign key constraint)
            em.createQuery("DELETE FROM ProductLot l WHERE l.supplier.id IN (:ids)")
                .setParameter("ids", List.of(supplierId1, supplierId2))
                .executeUpdate();
            
            // Delete products
            if (productId1 != null) {
                Product p1 = em.find(Product.class, productId1);
                if (p1 != null) em.remove(p1);
            }
            if (productId2 != null) {
                Product p2 = em.find(Product.class, productId2);
                if (p2 != null) em.remove(p2);
            }
            
            // Delete suppliers
            if (supplierId1 != null) {
                Supplier s1 = em.find(Supplier.class, supplierId1);
                if (s1 != null) em.remove(s1);
            }
            if (supplierId2 != null) {
                Supplier s2 = em.find(Supplier.class, supplierId2);
                if (s2 != null) em.remove(s2);
            }
        });
    }
}

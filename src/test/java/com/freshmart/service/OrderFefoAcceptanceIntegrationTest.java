package com.freshmart.service;

import com.freshmart.enums.OrderStatus;
import com.freshmart.util.JpaExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderFefoAcceptanceIntegrationTest {

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderService orderService = new OrderService();

    private String runKey;

    @BeforeEach
    void setUp() {
        runKey = "ITF" + UUID.randomUUID().toString().replace("-", "").substring(0,6);
    }

    @AfterEach
    void tearDown() {

        executor.executeVoid(em -> {

            em.createNativeQuery(
                    "DELETE FROM order_item_lot_allocations WHERE order_item_id IN (" +
                            "SELECT id FROM order_items WHERE order_id IN (" +
                            "SELECT id FROM orders WHERE order_code LIKE :codePrefix))"
            )
                    .setParameter("codePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM order_items WHERE order_id IN (" +
                            "SELECT id FROM orders WHERE order_code LIKE :codePrefix)"
            )
                    .setParameter("codePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM orders WHERE order_code LIKE :codePrefix"
            )
                    .setParameter("codePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM inventory_transactions WHERE product_lot_id IN (" +
                            "SELECT id FROM product_lots WHERE product_id IN (" +
                            "SELECT id FROM products WHERE name LIKE :namePrefix))"
            )
                    .setParameter("namePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM product_lots WHERE product_id IN (" +
                            "SELECT id FROM products WHERE name LIKE :namePrefix)"
            )
                    .setParameter("namePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM products WHERE name LIKE :namePrefix"
            )
                    .setParameter("namePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM revenue_daily WHERE revenue_date = CAST(GETDATE() AS DATE)"
            ).executeUpdate();
        });
    }

    // =====================================================
    // TEST 1 — FEFO nhiều lô
    // =====================================================

    @Test
    void completeOrder_shouldConsumeAcrossMultipleLotsByFEFO_forLargeSingleLineOrder() {

        Long productId = insertProduct("TOM_SU", new BigDecimal("100000"));

        Long lot1 = insertLot(productId, LocalDate.now().minusDays(5), LocalDate.now().plusDays(3), 30, new BigDecimal("70000"));
        Long lot2 = insertLot(productId, LocalDate.now().minusDays(4), LocalDate.now().plusDays(7), 40, new BigDecimal("71000"));
        Long lot3 = insertLot(productId, LocalDate.now().minusDays(3), LocalDate.now().plusDays(20), 50, new BigDecimal("72000"));

        Long orderId = insertOrder("PENDING");

        insertOrderItem(orderId, productId, 90, new BigDecimal("100000"));
        refreshOrderTotal(orderId);

        orderService.completeOrder(orderId);

        assertEquals("COMPLETED", getOrderStatus(orderId));
        assertTrue(hasCompletedAt(orderId));

        assertEquals(0, getQtyLeft(lot1));
        assertEquals(0, getQtyLeft(lot2));
        assertEquals(30, getQtyLeft(lot3));

        BigDecimal revenue = getTodayRevenue();
        assertTrue(revenue.compareTo(BigDecimal.ZERO) > 0);
    }


    // =====================================================
    // TEST 2 — workflow không trừ tồn trước khi completed
    // =====================================================

    @Test
    void workflowStatusUpdate_shouldNotConsumeInventoryBeforeCompleted_forMultiLineOrder() {

        Long productA = insertProduct("CA_HOI", new BigDecimal("200000"));
        Long productB = insertProduct("MUC_ONG", new BigDecimal("150000"));

        Long aLot1 = insertLot(productA, LocalDate.now().minusDays(10), LocalDate.now().plusDays(2), 20, new BigDecimal("120000"));
        Long aLot2 = insertLot(productA, LocalDate.now().minusDays(8), LocalDate.now().plusDays(8), 30, new BigDecimal("125000"));

        Long bLot1 = insertLot(productB, LocalDate.now().minusDays(7), LocalDate.now().plusDays(4), 10, new BigDecimal("90000"));
        Long bLot2 = insertLot(productB, LocalDate.now().minusDays(6), LocalDate.now().plusDays(12), 25, new BigDecimal("92000"));

        Long orderId = insertOrder("PENDING");

        insertOrderItem(orderId, productA, 35, new BigDecimal("200000"));
        insertOrderItem(orderId, productB, 30, new BigDecimal("150000"));
        refreshOrderTotal(orderId);

        orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING);
        assertEquals("PROCESSING", getOrderStatus(orderId));

        orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING);
        assertEquals("SHIPPING", getOrderStatus(orderId));

        // chưa trừ tồn
        assertEquals(20, getQtyLeft(aLot1));
        assertEquals(30, getQtyLeft(aLot2));
        assertEquals(10, getQtyLeft(bLot1));
        assertEquals(25, getQtyLeft(bLot2));

        orderService.completeOrder(orderId);

        assertEquals("COMPLETED", getOrderStatus(orderId));

        assertEquals(0, getQtyLeft(aLot1));
        assertEquals(15, getQtyLeft(aLot2));

        // ===== ADDED ASSERT (fix warning bLot1/bLot2) =====
        assertEquals(0, getQtyLeft(bLot1));
        assertEquals(5, getQtyLeft(bLot2));
        // ================================================

        BigDecimal revenue = getTodayRevenue();
        assertTrue(revenue.compareTo(BigDecimal.ZERO) > 0);
    }


    // =====================================================
    // TEST 3 — nhiều order share stock
    // =====================================================

    @Test
    void multipleOrders_shouldRespectSharedStock_andRejectSecondLargeOrderWhenInsufficient() {

        Long productId = insertProduct("CUA_BIEN", new BigDecimal("300000"));

        Long lot1 = insertLot(productId, LocalDate.now().minusDays(5), LocalDate.now().plusDays(2), 40, new BigDecimal("180000"));
        Long lot2 = insertLot(productId, LocalDate.now().minusDays(4), LocalDate.now().plusDays(5), 30, new BigDecimal("181000"));
        Long lot3 = insertLot(productId, LocalDate.now().minusDays(3), LocalDate.now().plusDays(15), 20, new BigDecimal("182000"));

        Long order1 = insertOrder("PENDING");
        insertOrderItem(order1, productId, 70, new BigDecimal("300000"));
        refreshOrderTotal(order1);

        Long order2 = insertOrder("PENDING");
        insertOrderItem(order2, productId, 35, new BigDecimal("300000"));
        refreshOrderTotal(order2);

        orderService.updateOrderStatus(order1, OrderStatus.PROCESSING);
        orderService.updateOrderStatus(order1, OrderStatus.SHIPPING);
        orderService.completeOrder(order1);

        assertEquals("COMPLETED", getOrderStatus(order1));

        assertEquals(0, getQtyLeft(lot1));
        assertEquals(0, getQtyLeft(lot2));
        assertEquals(20, getQtyLeft(lot3));

        orderService.updateOrderStatus(order2, OrderStatus.PROCESSING);
        orderService.updateOrderStatus(order2, OrderStatus.SHIPPING);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.completeOrder(order2));

        assertNotNull(ex.getMessage());
    }


    // =====================================================
    // HELPER
    // =====================================================

    private BigDecimal getTodayRevenue() {

        return executor.execute(em -> {

            Object value = em.createNativeQuery(
                    "SELECT COALESCE(SUM(total_revenue),0) " +
                            "FROM revenue_daily " +
                            "WHERE revenue_date = CAST(GETDATE() AS DATE)"
            ).getSingleResult();

            return (BigDecimal) value;
        });
    }

    private int getQtyLeft(Long lotId) {
        return executor.execute(em -> {
            Number value = (Number) em.createNativeQuery(
                    "SELECT qty_left FROM product_lots WHERE id = :lotId")
                    .setParameter("lotId", lotId)
                    .getSingleResult();
            return value.intValue();
        });
    }

    private String getOrderStatus(Long orderId) {
        return executor.execute(em -> {
            Object value = em.createNativeQuery(
                    "SELECT status FROM orders WHERE id = :orderId")
                    .setParameter("orderId", orderId)
                    .getSingleResult();
            return value.toString();
        });
    }

    private boolean hasCompletedAt(Long orderId) {
        return executor.execute(em -> {
            Object value = em.createNativeQuery(
                    "SELECT completed_at FROM orders WHERE id = :orderId")
                    .setParameter("orderId", orderId)
                    .getSingleResult();
            return value != null;
        });
    }

    // =====================================================
    // INSERT HELPERS
    // =====================================================

    private Long insertProduct(String suffix, BigDecimal sellPrice) {

        String name = runKey + "_P_" + suffix;

        return executor.execute(em -> {

            em.createNativeQuery(
                    "INSERT INTO products(name,category,unit,sell_price,active) " +
                            "VALUES(:name,'TEST','pcs',:price,1)")
                    .setParameter("name", name)
                    .setParameter("price", sellPrice)
                    .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                    "SELECT TOP 1 id FROM products WHERE name = :name ORDER BY id DESC")
                    .setParameter("name", name)
                    .getSingleResult();

            return id.longValue();
        });
    }

    private Long insertLot(Long productId,
                           LocalDate importDate,
                           LocalDate expiryDate,
                           int qty,
                           BigDecimal price) {

        return executor.execute(em -> {

            em.createNativeQuery(
                    "INSERT INTO product_lots(product_id, supplier_id, import_date, expiry_date, qty_in, qty_left, import_price) " +
                            "VALUES(:pid,NULL,:importDate,:expiryDate,:qty,:qty,:price)")
                    .setParameter("pid", productId)
                    .setParameter("importDate", Date.valueOf(importDate))
                    .setParameter("expiryDate", Date.valueOf(expiryDate))
                    .setParameter("qty", qty)
                    .setParameter("price", price)
                    .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                    "SELECT TOP 1 id FROM product_lots WHERE product_id = :pid ORDER BY id DESC")
                    .setParameter("pid", productId)
                    .getSingleResult();

            return id.longValue();
        });
    }

    private Long insertOrder(String status) {

        String code = runKey + "_O_" + UUID.randomUUID().toString().substring(0,6);

        return executor.execute(em -> {

            em.createNativeQuery(
                    "INSERT INTO orders(order_code,type,status,payment_method,total_amount,created_at) " +
                            "VALUES(:code,'ONLINE',:status,'COD',0,GETDATE())")
                    .setParameter("code", code)
                    .setParameter("status", status)
                    .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                    "SELECT TOP 1 id FROM orders WHERE order_code = :code ORDER BY id DESC")
                    .setParameter("code", code)
                    .getSingleResult();

            return id.longValue();
        });
    }

    private void insertOrderItem(Long orderId, Long productId, int qty, BigDecimal price) {

        executor.executeVoid(em -> {

            BigDecimal total = price.multiply(BigDecimal.valueOf(qty));

            em.createNativeQuery(
                    "INSERT INTO order_items(order_id,product_id,quantity,unit_price,line_total) " +
                            "VALUES(:oid,:pid,:qty,:price,:total)")
                    .setParameter("oid", orderId)
                    .setParameter("pid", productId)
                    .setParameter("qty", qty)
                    .setParameter("price", price)
                    .setParameter("total", total)
                    .executeUpdate();
        });
    }

    private void refreshOrderTotal(Long orderId) {

        executor.executeVoid(em -> {

            BigDecimal total = (BigDecimal) em.createNativeQuery(
                    "SELECT COALESCE(SUM(line_total),0) FROM order_items WHERE order_id = :oid")
                    .setParameter("oid", orderId)
                    .getSingleResult();

            em.createNativeQuery(
                    "UPDATE orders SET total_amount = :total WHERE id = :oid")
                    .setParameter("total", total)
                    .setParameter("oid", orderId)
                    .executeUpdate();
        });
    }
}
package com.freshmart.service;

import com.freshmart.enums.OrderStatus;
import com.freshmart.util.JpaExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderFefoAcceptanceIntegrationTest {

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderService orderService = new OrderService();

    private String runKey;

    @BeforeEach
    void setUp() {
        runKey = "ITF" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    @AfterEach
    void tearDown() {
        executor.executeVoid(em -> {
            em.createNativeQuery(
                    "DELETE FROM order_items WHERE order_id IN (" +
                            "SELECT id FROM orders WHERE order_code LIKE :codePrefix" +
                            ")"
            ).setParameter("codePrefix", runKey + "%").executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM orders WHERE order_code LIKE :codePrefix"
            ).setParameter("codePrefix", runKey + "%").executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM product_lots WHERE product_id IN (" +
                            "SELECT id FROM products WHERE name LIKE :namePrefix" +
                            ")"
            ).setParameter("namePrefix", runKey + "%").executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM products WHERE name LIKE :namePrefix"
            ).setParameter("namePrefix", runKey + "%").executeUpdate();
        });
    }

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
    }

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

        assertEquals(20, getQtyLeft(aLot1));
        assertEquals(30, getQtyLeft(aLot2));
        assertEquals(10, getQtyLeft(bLot1));
        assertEquals(25, getQtyLeft(bLot2));

        orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING);
        assertEquals("SHIPPING", getOrderStatus(orderId));

        assertEquals(20, getQtyLeft(aLot1));
        assertEquals(30, getQtyLeft(aLot2));
        assertEquals(10, getQtyLeft(bLot1));
        assertEquals(25, getQtyLeft(bLot2));

        orderService.completeOrder(orderId);
        assertEquals("COMPLETED", getOrderStatus(orderId));
        assertTrue(hasCompletedAt(orderId));

        assertEquals(0, getQtyLeft(aLot1));
        assertEquals(15, getQtyLeft(aLot2));

        assertEquals(0, getQtyLeft(bLot1));
        assertEquals(5, getQtyLeft(bLot2));
    }

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

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.completeOrder(order2));
        assertNotNull(ex.getMessage());

        assertEquals("SHIPPING", getOrderStatus(order2));
        assertEquals(20, getQtyLeft(lot3));
    }

    private Long insertProduct(String suffix, BigDecimal sellPrice) {
        String productName = runKey + "_P_" + suffix;

        return executor.execute(em -> {
            em.createNativeQuery(
                    "INSERT INTO products(name, category, unit, sell_price, image_url, description, active) " +
                            "VALUES (:name, :category, :unit, :price, NULL, :description, 1)"
            )
                    .setParameter("name", productName)
                    .setParameter("category", "TEST")
                    .setParameter("unit", "pcs")
                    .setParameter("price", sellPrice)
                    .setParameter("description", "integration test")
                    .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                            "SELECT TOP 1 id FROM products WHERE name = :name ORDER BY id DESC"
                    )
                    .setParameter("name", productName)
                    .getSingleResult();

            return id.longValue();
        });
    }

    private Long insertLot(Long productId,
                           LocalDate importDate,
                           LocalDate expiryDate,
                           int qty,
                           BigDecimal importPrice) {

        return executor.execute(em -> {
            em.createNativeQuery(
                    "INSERT INTO product_lots(product_id, supplier_id, import_date, expiry_date, qty_in, qty_left, import_price) " +
                            "VALUES (:productId, NULL, :importDate, :expiryDate, :qtyIn, :qtyLeft, :importPrice)"
            )
                    .setParameter("productId", productId)
                    .setParameter("importDate", Date.valueOf(importDate))
                    .setParameter("expiryDate", Date.valueOf(expiryDate))
                    .setParameter("qtyIn", qty)
                    .setParameter("qtyLeft", qty)
                    .setParameter("importPrice", importPrice)
                    .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                            "SELECT TOP 1 id FROM product_lots " +
                                    "WHERE product_id = :productId AND expiry_date = :expiryDate AND qty_in = :qtyIn " +
                                    "ORDER BY id DESC"
                    )
                    .setParameter("productId", productId)
                    .setParameter("expiryDate", Date.valueOf(expiryDate))
                    .setParameter("qtyIn", qty)
                    .getSingleResult();

            return id.longValue();
        });
    }

    private Long insertOrder(String status) {
        String orderCode = runKey + "_O_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        return executor.execute(em -> {
            em.createNativeQuery(
                    "INSERT INTO orders(order_code, customer_id, created_by, type, status, payment_method, total_amount, created_at, completed_at) " +
                            "VALUES (:orderCode, NULL, NULL, :type, :status, :paymentMethod, :totalAmount, :createdAt, NULL)"
            )
                    .setParameter("orderCode", orderCode)
                    .setParameter("type", "ONLINE")
                    .setParameter("status", status)
                    .setParameter("paymentMethod", "COD")
                    .setParameter("totalAmount", BigDecimal.ZERO)
                    .setParameter("createdAt", Timestamp.valueOf(LocalDateTime.now()))
                    .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                            "SELECT TOP 1 id FROM orders WHERE order_code = :orderCode ORDER BY id DESC"
                    )
                    .setParameter("orderCode", orderCode)
                    .getSingleResult();

            return id.longValue();
        });
    }

    private void insertOrderItem(Long orderId, Long productId, int qty, BigDecimal unitPrice) {
        executor.executeVoid(em -> {
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            em.createNativeQuery(
                    "INSERT INTO order_items(order_id, product_id, quantity, unit_price, line_total) " +
                            "VALUES (:orderId, :productId, :quantity, :unitPrice, :lineTotal)"
            )
                    .setParameter("orderId", orderId)
                    .setParameter("productId", productId)
                    .setParameter("quantity", qty)
                    .setParameter("unitPrice", unitPrice)
                    .setParameter("lineTotal", lineTotal)
                    .executeUpdate();
        });
    }

    private void refreshOrderTotal(Long orderId) {
        executor.executeVoid(em -> {
            BigDecimal total = (BigDecimal) em.createNativeQuery(
                            "SELECT COALESCE(SUM(line_total), 0) FROM order_items WHERE order_id = :orderId"
                    )
                    .setParameter("orderId", orderId)
                    .getSingleResult();

            em.createNativeQuery(
                    "UPDATE orders SET total_amount = :total WHERE id = :orderId"
            )
                    .setParameter("total", total)
                    .setParameter("orderId", orderId)
                    .executeUpdate();
        });
    }

    private int getQtyLeft(Long lotId) {
        return executor.execute(em -> {
            Number value = (Number) em.createNativeQuery(
                            "SELECT qty_left FROM product_lots WHERE id = :lotId"
                    )
                    .setParameter("lotId", lotId)
                    .getSingleResult();
            return value.intValue();
        });
    }

    private String getOrderStatus(Long orderId) {
        return executor.execute(em -> {
            Object value = em.createNativeQuery(
                            "SELECT status FROM orders WHERE id = :orderId"
                    )
                    .setParameter("orderId", orderId)
                    .getSingleResult();
            return value.toString();
        });
    }

    private boolean hasCompletedAt(Long orderId) {
        return executor.execute(em -> {
            Object value = em.createNativeQuery(
                            "SELECT completed_at FROM orders WHERE id = :orderId"
                    )
                    .setParameter("orderId", orderId)
                    .getSingleResult();
            return value != null;
        });
    }
}
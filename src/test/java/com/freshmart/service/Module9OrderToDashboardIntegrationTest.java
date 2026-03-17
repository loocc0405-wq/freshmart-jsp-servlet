package com.freshmart.service;

import com.freshmart.service.dto.ForecastPoint;
import com.freshmart.service.dto.ReplenishSuggestion;
import com.freshmart.service.dto.SeasonalityPoint;
import com.freshmart.util.JpaExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Module9OrderToDashboardIntegrationTest {

    private final JpaExecutor executor = new JpaExecutor();

    private final OrderService orderService = new OrderService();
    private final ForecastService forecastService = new ForecastService();
    private final SeasonalityService seasonalityService = new SeasonalityService();
    private final ReplenishmentService replenishmentService = new ReplenishmentService();

    private String runKey;
    private BigDecimal originalTodayRevenue;
    private boolean originalTodayRevenueExists;

    @BeforeEach
    void setUp() {
        runKey = "M9E2E_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        snapshotTodayRevenue();
    }

    @AfterEach
    void tearDown() {
        restoreTodayRevenue();

        executor.executeVoid(em -> {
            em.createNativeQuery(
                    "DELETE FROM order_items WHERE order_id IN (" +
                            "SELECT id FROM orders WHERE order_code LIKE :codePrefix)")
                    .setParameter("codePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM orders WHERE order_code LIKE :codePrefix")
                    .setParameter("codePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM product_lots WHERE product_id IN (" +
                            "SELECT id FROM products WHERE name LIKE :namePrefix)")
                    .setParameter("namePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM products WHERE name LIKE :namePrefix")
                    .setParameter("namePrefix", runKey + "%")
                    .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM suppliers WHERE name LIKE :namePrefix")
                    .setParameter("namePrefix", runKey + "%")
                    .executeUpdate();
        });
    }

    @Test
    void completeOrder_shouldUpdateRevenueAndFeedModule9Services() {
        BigDecimal price = new BigDecimal("100000.00");

        Long supplierId = insertSupplier("NCC_MODULE9", 2);
        Long productId = insertProduct("MUT_MANG_CAU", price);
        Long lotId = insertLot(
                productId,
                supplierId,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(15),
                5,
                new BigDecimal("70000.00")
        );

        Long orderId = insertOrder("PENDING");
        insertOrderItem(orderId, productId, 4, price);
        refreshOrderTotal(orderId);

        BigDecimal beforeRevenue = getTodayRevenue();

        orderService.completeOrder(orderId);

        BigDecimal afterRevenue = getTodayRevenue();

        assertEquals("COMPLETED", getOrderStatus(orderId));
        assertTrue(hasCompletedAt(orderId));
        assertEquals(1, getQtyLeft(lotId));

        BigDecimal delta = afterRevenue.subtract(beforeRevenue);
        assertEquals(0, new BigDecimal("400000.00").compareTo(delta));

        List<ForecastPoint> forecast = forecastService.forecastMovingAverage(1, 1, 1);
        assertEquals(2, forecast.size());
        assertEquals(LocalDate.now(), forecast.get(0).getDate());
        assertNotNull(forecast.get(0).getActual());
        assertTrue(forecast.get(0).getActual().compareTo(afterRevenue) >= 0);
        assertNotNull(forecast.get(1).getForecast());

        List<SeasonalityPoint> seasonality = seasonalityService.analyze(1, 1, 1.0);
        assertEquals(1, seasonality.size());
        assertEquals(LocalDate.now(), seasonality.get(0).getDate());
        assertNotNull(seasonality.get(0).getActual());

        List<ReplenishSuggestion> suggestions = replenishmentService.suggest(7, 2, 1, 1);
        ReplenishSuggestion row = findSuggestionByProductId(suggestions, productId);

        assertNotNull(row, "Replenishment must contain the completed-order product");
        assertEquals(productId, row.getProductId());
        assertTrue(row.getForecastPerDay().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(row.getExpectedDemand().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(row.getReorderPoint().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(row.getSuggestedQty() > 0, "Low remaining stock should trigger restock suggestion");

        assertEquals(supplierId, row.getRecommendedSupplierId());
        assertEquals("Best supplier: " + runKey + "_S_NCC_MODULE9 (Lead: 2d, Avg price: 70000.00, Lots: 1)",
                row.getRecommendationReason());
    }

    private ReplenishSuggestion findSuggestionByProductId(List<ReplenishSuggestion> suggestions, Long productId) {
        for (ReplenishSuggestion s : suggestions) {
            if (productId.equals(s.getProductId())) {
                return s;
            }
        }
        return null;
    }

    private void snapshotTodayRevenue() {
        executor.executeVoid(em -> {
            @SuppressWarnings("unchecked")
            List<Object> rows = em.createNativeQuery(
                    "SELECT total_revenue FROM revenue_daily WHERE revenue_date = CAST(GETDATE() AS DATE)")
                    .getResultList();

            if (rows.isEmpty()) {
                originalTodayRevenueExists = false;
                originalTodayRevenue = BigDecimal.ZERO;
            } else {
                originalTodayRevenueExists = true;
                originalTodayRevenue = (BigDecimal) rows.get(0);
            }
        });
    }

    private void restoreTodayRevenue() {
        executor.executeVoid(em -> {
            if (originalTodayRevenueExists) {
                Number count = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM revenue_daily WHERE revenue_date = CAST(GETDATE() AS DATE)")
                        .getSingleResult();

                if (count.intValue() > 0) {
                    em.createNativeQuery(
                            "UPDATE revenue_daily SET total_revenue = :value WHERE revenue_date = CAST(GETDATE() AS DATE)")
                            .setParameter("value", originalTodayRevenue)
                            .executeUpdate();
                } else {
                    em.createNativeQuery(
                            "INSERT INTO revenue_daily(revenue_date, total_revenue) VALUES (CAST(GETDATE() AS DATE), :value)")
                            .setParameter("value", originalTodayRevenue)
                            .executeUpdate();
                }
            } else {
                em.createNativeQuery(
                        "DELETE FROM revenue_daily WHERE revenue_date = CAST(GETDATE() AS DATE)")
                        .executeUpdate();
            }
        });
    }

    private BigDecimal getTodayRevenue() {
        return executor.execute(em -> {
            Object value = em.createNativeQuery(
                    "SELECT COALESCE(SUM(total_revenue), 0) " +
                            "FROM revenue_daily " +
                            "WHERE revenue_date = CAST(GETDATE() AS DATE)")
                    .getSingleResult();

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

    private Long insertSupplier(String suffix, int leadTimeDays) {
        String name = runKey + "_S_" + suffix;
        String email = runKey.toLowerCase() + "_" + suffix.toLowerCase() + "@test.local";

        return executor.execute(em -> {
            em.createNativeQuery(
                    "INSERT INTO suppliers(name, email, phone, address, certificate, lead_time_days, note) " +
                            "VALUES(:name, :email, '0123456789', 'Test address', 'VietGAP', :lead, 'module9 test')")
                    .setParameter("name", name)
                    .setParameter("email", email)
                    .setParameter("lead", leadTimeDays)
                    .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                    "SELECT TOP 1 id FROM suppliers WHERE name = :name ORDER BY id DESC")
                    .setParameter("name", name)
                    .getSingleResult();

            return id.longValue();
        });
    }

    private Long insertProduct(String suffix, BigDecimal sellPrice) {
        String name = runKey + "_P_" + suffix;

        return executor.execute(em -> {
            em.createNativeQuery(
                    "INSERT INTO products(name, category, unit, sell_price, active) " +
                            "VALUES(:name, 'TEST', 'pcs', :price, 1)")
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
                           Long supplierId,
                           LocalDate importDate,
                           LocalDate expiryDate,
                           int qty,
                           BigDecimal price) {

        return executor.execute(em -> {
            em.createNativeQuery(
                    "INSERT INTO product_lots(product_id, supplier_id, import_date, expiry_date, qty_in, qty_left, import_price) " +
                            "VALUES(:pid, :sid, :importDate, :expiryDate, :qtyIn, :qtyLeft, :price)")
                    .setParameter("pid", productId)
                    .setParameter("sid", supplierId)
                    .setParameter("importDate", Date.valueOf(importDate))
                    .setParameter("expiryDate", Date.valueOf(expiryDate))
                    .setParameter("qtyIn", qty)
                    .setParameter("qtyLeft", qty)
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
        String code = runKey + "_O_" + UUID.randomUUID().toString().substring(0, 6);

        return executor.execute(em -> {
            em.createNativeQuery(
                    "INSERT INTO orders(order_code, type, status, payment_method, total_amount, created_at) " +
                            "VALUES(:code, 'ONLINE', :status, 'COD', 0, GETDATE())")
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
                    "INSERT INTO order_items(order_id, product_id, quantity, unit_price, line_total) " +
                            "VALUES(:oid, :pid, :qty, :price, :total)")
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
                    "SELECT COALESCE(SUM(line_total), 0) FROM order_items WHERE order_id = :oid")
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
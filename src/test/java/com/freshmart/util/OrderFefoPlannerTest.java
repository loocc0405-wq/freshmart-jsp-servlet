package com.freshmart.util;

import com.freshmart.entity.ProductLot;
import com.freshmart.service.dto.FefoAllocationPlan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderFefoPlannerTest {

    @Test
    void buildPlan_allocatesInFefoOrderAndFlagsNearExpiry() {
        LocalDate today = LocalDate.of(2026, 3, 9);

        ProductLot late = lot(2L, today.minusDays(3), today.plusDays(5), 10);
        ProductLot early = lot(1L, today.minusDays(5), today.plusDays(1), 4);

        FefoAllocationPlan plan = OrderFefoPlanner.buildPlan(List.of(late, early), 6, today, 3);

        assertTrue(plan.isEnoughStock());
        assertEquals(14, plan.getAvailableQty());
        assertEquals(4, plan.getNearExpiryQty());
        assertTrue(plan.isUsesNearExpiryLots());
        assertEquals(2, plan.getAllocations().size());
        assertEquals(1L, plan.getAllocations().get(0).getLotId());
        assertEquals(4, plan.getAllocations().get(0).getAllocatedQty());
        assertEquals(2L, plan.getAllocations().get(1).getLotId());
        assertEquals(2, plan.getAllocations().get(1).getAllocatedQty());
    }

    @Test
    void buildPlan_reportsShortageWhenAvailableNotEnough() {
        LocalDate today = LocalDate.of(2026, 3, 9);
        ProductLot single = lot(10L, today.minusDays(1), today.plusDays(2), 3);

        FefoAllocationPlan plan = OrderFefoPlanner.buildPlan(List.of(single), 5, today, 3);

        assertFalse(plan.isEnoughStock());
        assertEquals(2, plan.getShortageQty());
        assertEquals(3, plan.getAllocations().get(0).getAllocatedQty());
    }

    private ProductLot lot(Long id, LocalDate importDate, LocalDate expiryDate, int qtyLeft) {
        ProductLot lot = new ProductLot();
        try {
            java.lang.reflect.Field field = ProductLot.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(lot, id);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        lot.setImportDate(importDate);
        lot.setExpiryDate(expiryDate);
        lot.setQtyIn(qtyLeft);
        lot.setQtyLeft(qtyLeft);
        return lot;
    }
}

package com.freshmart.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplenishmentServiceInputTest {

    @Test
    void getSeasonFactor_shouldBeNormalOnRegularWeekday() throws Exception {
        ReplenishmentService service = new ReplenishmentService();
        Method method = ReplenishmentService.class.getDeclaredMethod("getSeasonFactor", LocalDate.class);
        method.setAccessible(true);

        BigDecimal factor = (BigDecimal) method.invoke(service, LocalDate.of(2026, 3, 11));

        assertEquals(new BigDecimal("1.00"), factor);
    }

    @Test
    void getSeasonFactor_shouldIncreaseInHotSeason() throws Exception {
        ReplenishmentService service = new ReplenishmentService();
        Method method = ReplenishmentService.class.getDeclaredMethod("getSeasonFactor", LocalDate.class);
        method.setAccessible(true);

        BigDecimal factor = (BigDecimal) method.invoke(service, LocalDate.of(2026, 6, 15));

        assertEquals(new BigDecimal("1.15"), factor);
    }

    @Test
    void getSeasonFactor_shouldIncreaseInTetSeason() throws Exception {
        ReplenishmentService service = new ReplenishmentService();
        Method method = ReplenishmentService.class.getDeclaredMethod("getSeasonFactor", LocalDate.class);
        method.setAccessible(true);

        BigDecimal factor = (BigDecimal) method.invoke(service, LocalDate.of(2026, 1, 14));

        assertEquals(new BigDecimal("1.30"), factor);
    }

    @Test
    void getSeasonFactor_shouldCombineTetAndWeekendMultipliers() throws Exception {
        ReplenishmentService service = new ReplenishmentService();
        Method method = ReplenishmentService.class.getDeclaredMethod("getSeasonFactor", LocalDate.class);
        method.setAccessible(true);

        BigDecimal factor = (BigDecimal) method.invoke(service, LocalDate.of(2026, 1, 3));

        assertEquals(new BigDecimal("1.43"), factor);
    }

    @Test
    void getSeasonFactor_shouldCombineHotSeasonAndWeekendMultipliers() throws Exception {
        ReplenishmentService service = new ReplenishmentService();
        Method method = ReplenishmentService.class.getDeclaredMethod("getSeasonFactor", LocalDate.class);
        method.setAccessible(true);

        BigDecimal factor = (BigDecimal) method.invoke(service, LocalDate.of(2026, 6, 14));

        assertEquals(new BigDecimal("1.27"), factor);
    }
}
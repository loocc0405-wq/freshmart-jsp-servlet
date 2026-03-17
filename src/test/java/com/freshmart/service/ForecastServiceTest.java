package com.freshmart.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ForecastServiceTest {

    @Test
    void forecastMovingAverage_shouldThrowWhenDaysHistoryInvalid() {
        ForecastService service = new ForecastService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.forecastMovingAverage(0, 5, 3)
        );

        assertEquals("daysHistory must be > 0", ex.getMessage());
    }

    @Test
    void forecastMovingAverage_shouldThrowWhenHorizonInvalid() {
        ForecastService service = new ForecastService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.forecastMovingAverage(10, 0, 3)
        );

        assertEquals("horizon must be > 0", ex.getMessage());
    }

    @Test
    void forecastMovingAverage_shouldThrowWhenWindowInvalid() {
        ForecastService service = new ForecastService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.forecastMovingAverage(10, 5, 0)
        );

        assertEquals("window must be > 0", ex.getMessage());
    }

    @Test
    void forecastExponentialSmoothing_shouldThrowWhenDaysHistoryInvalid() {
        ForecastService service = new ForecastService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.forecastExponentialSmoothing(0, 5, 0.3)
        );

        assertEquals("daysHistory must be > 0", ex.getMessage());
    }

    @Test
    void forecastExponentialSmoothing_shouldThrowWhenHorizonInvalid() {
        ForecastService service = new ForecastService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.forecastExponentialSmoothing(10, 0, 0.3)
        );

        assertEquals("horizon must be > 0", ex.getMessage());
    }

    @Test
    void forecastExponentialSmoothing_shouldThrowWhenAlphaLessThanOrEqualZero() {
        ForecastService service = new ForecastService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.forecastExponentialSmoothing(10, 5, 0.0)
        );

        assertEquals("alpha must be in (0,1)", ex.getMessage());
    }

    @Test
    void forecastExponentialSmoothing_shouldThrowWhenAlphaGreaterThanOrEqualOne() {
        ForecastService service = new ForecastService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.forecastExponentialSmoothing(10, 5, 1.0)
        );

        assertEquals("alpha must be in (0,1)", ex.getMessage());
    }

    @Test
    void movingAverage_shouldAverageLastWindowValues() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("movingAverage", Map.class, int.class);
        method.setAccessible(true);

        Map<LocalDate, BigDecimal> series = new LinkedHashMap<>();
        series.put(LocalDate.of(2026, 3, 1), new BigDecimal("10"));
        series.put(LocalDate.of(2026, 3, 2), new BigDecimal("20"));
        series.put(LocalDate.of(2026, 3, 3), new BigDecimal("30"));
        series.put(LocalDate.of(2026, 3, 4), new BigDecimal("40"));

        BigDecimal result = (BigDecimal) method.invoke(service, series, 3);

        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    void movingAverage_shouldUseAllValuesWhenWindowGreaterThanSeriesSize() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("movingAverage", Map.class, int.class);
        method.setAccessible(true);

        Map<LocalDate, BigDecimal> series = new LinkedHashMap<>();
        series.put(LocalDate.of(2026, 3, 1), new BigDecimal("10"));
        series.put(LocalDate.of(2026, 3, 2), new BigDecimal("20"));
        series.put(LocalDate.of(2026, 3, 3), new BigDecimal("30"));

        BigDecimal result = (BigDecimal) method.invoke(service, series, 10);

        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    void movingAverage_shouldReturnZeroWhenSeriesEmpty() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("movingAverage", Map.class, int.class);
        method.setAccessible(true);

        Map<LocalDate, BigDecimal> series = new LinkedHashMap<>();

        BigDecimal result = (BigDecimal) method.invoke(service, series, 3);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void exponentialSmoothing_shouldReturnLastSmoothedValue() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("exponentialSmoothing", Map.class, double.class);
        method.setAccessible(true);

        Map<LocalDate, BigDecimal> series = new LinkedHashMap<>();
        series.put(LocalDate.of(2026, 3, 1), new BigDecimal("100"));
        series.put(LocalDate.of(2026, 3, 2), new BigDecimal("200"));
        series.put(LocalDate.of(2026, 3, 3), new BigDecimal("300"));

        BigDecimal result = (BigDecimal) method.invoke(service, series, 0.5d);

        // S1 = 100
        // S2 = 0.5*200 + 0.5*100 = 150
        // S3 = 0.5*300 + 0.5*150 = 225
        assertEquals(new BigDecimal("225.00"), result);
    }

    @Test
    void exponentialSmoothing_shouldReturnZeroWhenSeriesEmpty() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("exponentialSmoothing", Map.class, double.class);
        method.setAccessible(true);

        Map<LocalDate, BigDecimal> series = new LinkedHashMap<>();

        BigDecimal result = (BigDecimal) method.invoke(service, series, 0.3d);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void movingAverageOnBuckets_shouldAverageLastBuckets() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("movingAverageOnBuckets", LinkedHashMap.class, int.class);
        method.setAccessible(true);

        LinkedHashMap<String, BigDecimal> buckets = new LinkedHashMap<>();
        buckets.put("2026-01", new BigDecimal("100"));
        buckets.put("2026-02", new BigDecimal("200"));
        buckets.put("2026-03", new BigDecimal("300"));
        buckets.put("2026-04", new BigDecimal("400"));

        BigDecimal result = (BigDecimal) method.invoke(service, buckets, 2);

        assertEquals(new BigDecimal("350.00"), result);
    }

    @Test
    void exponentialSmoothingOnBuckets_shouldReturnLastSmoothedValue() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("exponentialSmoothingOnBuckets", LinkedHashMap.class, double.class);
        method.setAccessible(true);

        LinkedHashMap<String, BigDecimal> buckets = new LinkedHashMap<>();
        buckets.put("2026-01", new BigDecimal("100"));
        buckets.put("2026-02", new BigDecimal("200"));
        buckets.put("2026-03", new BigDecimal("300"));

        BigDecimal result = (BigDecimal) method.invoke(service, buckets, 0.5d);

        assertEquals(new BigDecimal("225.00"), result);
    }

    @Test
    void generateNextPeriods_shouldGenerateNextMonthsCorrectly() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("generateNextPeriods", String.class, String.class, int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(service, "month", "2026-11", 3);

        assertEquals(List.of("2026-12", "2027-01", "2027-02"), result);
    }

    @Test
    void generateNextPeriods_shouldGenerateNextQuartersCorrectly() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("generateNextPeriods", String.class, String.class, int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(service, "quarter", "2026-Q4", 3);

        assertEquals(List.of("2027-Q1", "2027-Q2", "2027-Q3"), result);
    }

    @Test
    void generateNextPeriods_shouldGenerateNextYearsCorrectly() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("generateNextPeriods", String.class, String.class, int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(service, "year", "2026", 3);

        assertEquals(List.of("2027", "2028", "2029"), result);
    }

    @Test
    void generateNextPeriods_shouldGenerateNextDaysCorrectly() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("generateNextPeriods", String.class, String.class, int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(service, "day", "2026-03-17", 3);

        assertEquals(List.of("2026-03-18", "2026-03-19", "2026-03-20"), result);
    }

    @Test
    void aggregate_shouldGroupDailySeriesIntoMonthlyBuckets() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("aggregate", Map.class, String.class);
        method.setAccessible(true);

        Map<LocalDate, BigDecimal> series = new LinkedHashMap<>();
        series.put(LocalDate.of(2026, 1, 1), new BigDecimal("100"));
        series.put(LocalDate.of(2026, 1, 15), new BigDecimal("50"));
        series.put(LocalDate.of(2026, 2, 1), new BigDecimal("200"));

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, BigDecimal> result =
                (LinkedHashMap<String, BigDecimal>) method.invoke(service, series, "month");

        assertEquals(new BigDecimal("150"), result.get("2026-01"));
        assertEquals(new BigDecimal("200"), result.get("2026-02"));
        assertEquals(2, result.size());
    }

    @Test
    void estimateDaysForHistory_shouldReturnExpectedRanges() throws Exception {
        ForecastService service = new ForecastService();
        Method method = ForecastService.class.getDeclaredMethod("estimateDaysForHistory", String.class, int.class);
        method.setAccessible(true);

        assertEquals(402, method.invoke(service, "month", 12));
        assertEquals(214, method.invoke(service, "quarter", 2));
        assertEquals(396, method.invoke(service, "year", 1));
        assertEquals(40, method.invoke(service, "day", 10));
    }
}
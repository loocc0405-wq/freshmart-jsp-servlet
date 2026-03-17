package com.freshmart.service;

import com.freshmart.service.dto.ForecastBucket;
import com.freshmart.service.dto.ForecastPoint;
import com.freshmart.util.JpaExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ForecastServiceIntegrationTest {

    private final JpaExecutor executor = new JpaExecutor();
    private final ForecastService service = new ForecastService();

    private LocalDate cleanupFrom;
    private LocalDate cleanupTo;

    @AfterEach
    void tearDown() {
        if (cleanupFrom != null && cleanupTo != null) {
            deleteRevenueRange(cleanupFrom, cleanupTo);
        }
    }

    @Test
    void forecastByGranularity_month_shouldGenerateFutureMonthLabelsCorrectly() {
        List<ForecastBucket> result = service.forecastByGranularity("month", "ma", 3, 2, 2, 0.3);

        assertTrue(result.size() >= 2);

        ForecastBucket next1 = result.get(result.size() - 2);
        ForecastBucket next2 = result.get(result.size() - 1);

        YearMonth now = YearMonth.now();
        assertEquals(now.plusMonths(1).toString(), next1.getLabel());
        assertEquals(now.plusMonths(2).toString(), next2.getLabel());

        assertNull(next1.getActual());
        assertNull(next2.getActual());
        assertNotNull(next1.getForecast());
        assertNotNull(next2.getForecast());
    }

    @Test
    void forecastByGranularity_quarter_shouldGenerateFutureQuarterLabelsCorrectly() {
        List<ForecastBucket> result = service.forecastByGranularity("quarter", "ma", 2, 2, 2, 0.3);

        assertTrue(result.size() >= 2);

        ForecastBucket next1 = result.get(result.size() - 2);
        ForecastBucket next2 = result.get(result.size() - 1);

        assertEquals(nextQuarterLabel(LocalDate.now(), 1), next1.getLabel());
        assertEquals(nextQuarterLabel(LocalDate.now(), 2), next2.getLabel());

        assertNull(next1.getActual());
        assertNull(next2.getActual());
        assertNotNull(next1.getForecast());
        assertNotNull(next2.getForecast());
    }

    @Test
    void forecastByGranularity_year_shouldGenerateFutureYearLabelsCorrectly() {
        List<ForecastBucket> result = service.forecastByGranularity("year", "ma", 2, 2, 2, 0.3);

        assertTrue(result.size() >= 2);

        ForecastBucket next1 = result.get(result.size() - 2);
        ForecastBucket next2 = result.get(result.size() - 1);

        int year = LocalDate.now().getYear();
        assertEquals(String.valueOf(year + 1), next1.getLabel());
        assertEquals(String.valueOf(year + 2), next2.getLabel());

        assertNull(next1.getActual());
        assertNull(next2.getActual());
        assertNotNull(next1.getForecast());
        assertNotNull(next2.getForecast());
    }

    @Test
    void forecastMovingAverage_shouldHandleWindowLargerThanHistory() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(2);

        cleanupFrom = from;
        cleanupTo = today;

        replaceRevenueRange(from, today, Map.of(
                today.minusDays(2), new BigDecimal("10.00"),
                today.minusDays(1), new BigDecimal("20.00"),
                today, new BigDecimal("30.00")
        ));

        List<ForecastPoint> result = service.forecastMovingAverage(3, 2, 10);

        assertEquals(5, result.size());

        ForecastPoint forecast1 = result.get(3);
        ForecastPoint forecast2 = result.get(4);

        assertEquals(new BigDecimal("20.00"), forecast1.getForecast());
        assertEquals(new BigDecimal("20.00"), forecast2.getForecast());
    }

    @Test
    void forecastExponentialSmoothing_shouldHandleAllZeroSeries() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(4);

        cleanupFrom = from;
        cleanupTo = today;

        deleteRevenueRange(from, today);

        List<ForecastPoint> result = service.forecastExponentialSmoothing(5, 2, 0.3);

        assertEquals(7, result.size());

        for (int i = 0; i < 5; i++) {
            assertEquals(BigDecimal.ZERO.setScale(2), result.get(i).getActual().setScale(2));
            assertNull(result.get(i).getForecast());
        }

        assertEquals(new BigDecimal("0.00"), result.get(5).getForecast());
        assertEquals(new BigDecimal("0.00"), result.get(6).getForecast());
    }

    private void replaceRevenueRange(LocalDate from, LocalDate to, Map<LocalDate, BigDecimal> values) {
        deleteRevenueRange(from, to);

        executor.executeVoid(em -> {
            for (Map.Entry<LocalDate, BigDecimal> e : values.entrySet()) {
                em.createNativeQuery(
                                "INSERT INTO revenue_daily(revenue_date, total_revenue) VALUES (:d, :v)")
                        .setParameter("d", Date.valueOf(e.getKey()))
                        .setParameter("v", e.getValue())
                        .executeUpdate();
            }
        });
    }

    private void deleteRevenueRange(LocalDate from, LocalDate to) {
        executor.executeVoid(em ->
                em.createNativeQuery(
                                "DELETE FROM revenue_daily WHERE revenue_date BETWEEN :from AND :to")
                        .setParameter("from", Date.valueOf(from))
                        .setParameter("to", Date.valueOf(to))
                        .executeUpdate()
        );
    }

    private String nextQuarterLabel(LocalDate base, int plusQuarters) {
        int quarter = ((base.getMonthValue() - 1) / 3) + 1;
        int year = base.getYear();

        for (int i = 0; i < plusQuarters; i++) {
            quarter++;
            if (quarter > 4) {
                quarter = 1;
                year++;
            }
        }

        return year + "-Q" + quarter;
    }
}
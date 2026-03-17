package com.freshmart.service;

import com.freshmart.service.dto.SeasonalityPoint;
import com.freshmart.util.JpaExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SeasonalityServiceIntegrationTest {

    private final JpaExecutor executor = new JpaExecutor();
    private final SeasonalityService service = new SeasonalityService();

    private LocalDate cleanupFrom;
    private LocalDate cleanupTo;

    @AfterEach
    void tearDown() {
        if (cleanupFrom != null && cleanupTo != null) {
            deleteRevenueRange(cleanupFrom, cleanupTo);
        }
    }

    @Test
    void analyze_shouldMarkPeakWhenZAboveThreshold() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(4);

        cleanupFrom = from;
        cleanupTo = today;

        replaceRevenueRange(from, today, Map.of(
                today.minusDays(4), new BigDecimal("10.00"),
                today.minusDays(3), new BigDecimal("10.00"),
                today.minusDays(2), new BigDecimal("10.00"),
                today.minusDays(1), new BigDecimal("10.00"),
                today, new BigDecimal("100.00")
        ));

        List<SeasonalityPoint> result = service.analyze(5, 5, 1.0);

        assertEquals(5, result.size());

        SeasonalityPoint last = result.get(4);
        assertEquals(today, last.getDate());
        assertEquals("PEAK", last.getSignal());
        assertTrue(last.getZScore() >= 1.0);
    }

    @Test
    void analyze_shouldMarkDipWhenZBelowNegativeThreshold() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(4);

        cleanupFrom = from;
        cleanupTo = today;

        replaceRevenueRange(from, today, Map.of(
                today.minusDays(4), new BigDecimal("100.00"),
                today.minusDays(3), new BigDecimal("100.00"),
                today.minusDays(2), new BigDecimal("100.00"),
                today.minusDays(1), new BigDecimal("100.00"),
                today, new BigDecimal("0.00")
        ));

        List<SeasonalityPoint> result = service.analyze(5, 5, 1.0);

        assertEquals(5, result.size());

        SeasonalityPoint last = result.get(4);
        assertEquals(today, last.getDate());
        assertEquals("DIP", last.getSignal());
        assertTrue(last.getZScore() <= -1.0);
    }

    @Test
    void analyze_shouldReturnBlankSignalWhenStdIsZero() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(4);

        cleanupFrom = from;
        cleanupTo = today;

        replaceRevenueRange(from, today, Map.of(
                today.minusDays(4), new BigDecimal("50.00"),
                today.minusDays(3), new BigDecimal("50.00"),
                today.minusDays(2), new BigDecimal("50.00"),
                today.minusDays(1), new BigDecimal("50.00"),
                today, new BigDecimal("50.00")
        ));

        List<SeasonalityPoint> result = service.analyze(5, 5, 1.0);

        SeasonalityPoint last = result.get(4);
        assertEquals(new BigDecimal("50.00"), last.getActual());
        assertEquals(new BigDecimal("50.00"), last.getRollingMean());
        assertEquals(new BigDecimal("0.00"), last.getRollingStd());
        assertEquals(0.0, last.getZScore(), 0.0001);
        assertEquals("", last.getSignal());
    }

    @Test
    void analyze_shouldFillMissingDatesWithZero() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(4);

        cleanupFrom = from;
        cleanupTo = today;

        replaceRevenueRange(from, today, Map.of(
                today, new BigDecimal("100.00")
        ));

        List<SeasonalityPoint> result = service.analyze(5, 5, 1.0);

        assertEquals(5, result.size());

        assertEquals(new BigDecimal("0.00"), result.get(0).getActual());
        assertEquals(new BigDecimal("0.00"), result.get(1).getActual());
        assertEquals(new BigDecimal("0.00"), result.get(2).getActual());
        assertEquals(new BigDecimal("0.00"), result.get(3).getActual());
        assertEquals(new BigDecimal("100.00"), result.get(4).getActual());
    }

    @Test
    void analyze_shouldUseRollingWindowCorrectly() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(4);

        cleanupFrom = from;
        cleanupTo = today;

        replaceRevenueRange(from, today, Map.of(
                today.minusDays(4), new BigDecimal("0.00"),
                today.minusDays(3), new BigDecimal("0.00"),
                today.minusDays(2), new BigDecimal("90.00"),
                today.minusDays(1), new BigDecimal("90.00"),
                today, new BigDecimal("90.00")
        ));

        List<SeasonalityPoint> result = service.analyze(5, 3, 10.0);

        SeasonalityPoint last = result.get(4);

        // nếu dùng đúng rolling window = 3 thì mean của 3 ngày cuối là 90
        assertEquals(new BigDecimal("90.00"), last.getRollingMean());
        assertEquals(new BigDecimal("0.00"), last.getRollingStd());
        assertEquals("", last.getSignal());
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
}
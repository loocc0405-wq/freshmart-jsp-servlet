package com.freshmart.service;

import com.freshmart.service.dto.SeasonalityMonthStat;
import com.freshmart.service.dto.SeasonalityPoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeasonalityServiceTest {

    @Test
    void analyze_shouldThrowWhenDaysHistoryInvalid() {
        SeasonalityService service = new SeasonalityService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.analyze(0, 7, 2.0)
        );

        assertEquals("daysHistory must be > 0", ex.getMessage());
    }

    @Test
    void analyze_shouldThrowWhenWindowInvalid() {
        SeasonalityService service = new SeasonalityService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.analyze(30, 0, 2.0)
        );

        assertEquals("window must be > 0", ex.getMessage());
    }

    @Test
    void analyze_shouldThrowWhenZThresholdInvalid() {
        SeasonalityService service = new SeasonalityService();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.analyze(30, 7, 0.0)
        );

        assertEquals("zThreshold must be > 0", ex.getMessage());
    }

    @Test
    void summarizeByMonth_shouldGroupAndComputeAvgMinMaxCorrectly() {
        SeasonalityService service = new SeasonalityService();

        List<SeasonalityPoint> points = List.of(
                new SeasonalityPoint(
                        LocalDate.of(2026, 1, 5),
                        new BigDecimal("100"),
                        new BigDecimal("90"),
                        new BigDecimal("5"),
                        2.0,
                        "PEAK"
                ),
                new SeasonalityPoint(
                        LocalDate.of(2026, 1, 10),
                        new BigDecimal("200"),
                        new BigDecimal("120"),
                        new BigDecimal("10"),
                        1.5,
                        ""
                ),
                new SeasonalityPoint(
                        LocalDate.of(2026, 2, 3),
                        new BigDecimal("50"),
                        new BigDecimal("70"),
                        new BigDecimal("8"),
                        -2.1,
                        "DIP"
                ),
                new SeasonalityPoint(
                        LocalDate.of(2026, 2, 8),
                        new BigDecimal("150"),
                        new BigDecimal("100"),
                        new BigDecimal("12"),
                        1.0,
                        ""
                )
        );

        List<SeasonalityMonthStat> result = service.summarizeByMonth(points);

        assertEquals(2, result.size());

        SeasonalityMonthStat jan = result.get(0);
        assertEquals(1, jan.getMonth());
        assertEquals("Month 1", jan.getLabel());
        assertEquals(new BigDecimal("150.00"), jan.getAvgDemand());
        assertEquals(new BigDecimal("100"), jan.getMinDemand());
        assertEquals(new BigDecimal("200"), jan.getMaxDemand());

        SeasonalityMonthStat feb = result.get(1);
        assertEquals(2, feb.getMonth());
        assertEquals("Month 2", feb.getLabel());
        assertEquals(new BigDecimal("100.00"), feb.getAvgDemand());
        assertEquals(new BigDecimal("50"), feb.getMinDemand());
        assertEquals(new BigDecimal("150"), feb.getMaxDemand());
    }

    @Test
    void summarizeByMonth_shouldTreatNullActualAsZero() {
        SeasonalityService service = new SeasonalityService();

        List<SeasonalityPoint> points = List.of(
                new SeasonalityPoint(
                        LocalDate.of(2026, 3, 1),
                        null,
                        new BigDecimal("0"),
                        new BigDecimal("0"),
                        0.0,
                        ""
                ),
                new SeasonalityPoint(
                        LocalDate.of(2026, 3, 2),
                        new BigDecimal("30"),
                        new BigDecimal("10"),
                        new BigDecimal("5"),
                        1.0,
                        ""
                )
        );

        List<SeasonalityMonthStat> result = service.summarizeByMonth(points);

        assertEquals(1, result.size());

        SeasonalityMonthStat mar = result.get(0);
        assertEquals(3, mar.getMonth());
        assertEquals(new BigDecimal("15.00"), mar.getAvgDemand());
        assertEquals(BigDecimal.ZERO, mar.getMinDemand());
        assertEquals(new BigDecimal("30"), mar.getMaxDemand());
    }

    @Test
    void summarizeByMonth_shouldReturnEmptyWhenInputEmpty() {
        SeasonalityService service = new SeasonalityService();

        List<SeasonalityMonthStat> result = service.summarizeByMonth(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void summarizeByMonth_shouldSortByMonthAscending() {
        SeasonalityService service = new SeasonalityService();

        List<SeasonalityPoint> points = List.of(
                new SeasonalityPoint(
                        LocalDate.of(2026, 12, 1),
                        new BigDecimal("120"),
                        new BigDecimal("100"),
                        new BigDecimal("10"),
                        1.0,
                        ""
                ),
                new SeasonalityPoint(
                        LocalDate.of(2026, 1, 1),
                        new BigDecimal("80"),
                        new BigDecimal("70"),
                        new BigDecimal("5"),
                        1.0,
                        ""
                )
        );

        List<SeasonalityMonthStat> result = service.summarizeByMonth(points);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getMonth());
        assertEquals(12, result.get(1).getMonth());
    }

    @Test
    void summarizeByMonth_shouldKeepSinglePointMonthCorrectly() {
        SeasonalityService service = new SeasonalityService();

        List<SeasonalityPoint> points = List.of(
                new SeasonalityPoint(
                        LocalDate.of(2026, 4, 10),
                        new BigDecimal("75"),
                        new BigDecimal("70"),
                        new BigDecimal("2"),
                        2.5,
                        "PEAK"
                )
        );

        List<SeasonalityMonthStat> result = service.summarizeByMonth(points);

        assertEquals(1, result.size());

        SeasonalityMonthStat apr = result.get(0);
        assertEquals(4, apr.getMonth());
        assertEquals(new BigDecimal("75.00"), apr.getAvgDemand());
        assertEquals(new BigDecimal("75"), apr.getMinDemand());
        assertEquals(new BigDecimal("75"), apr.getMaxDemand());
    }
}
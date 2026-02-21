package com.freshmart.service;

import com.freshmart.entity.RevenueDaily;
import com.freshmart.repository.RevenueDailyRepository;
import com.freshmart.service.dto.ForecastPoint;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Module 9 - Revenue Forecasting (No AI):
 * - Moving Average
 * - Exponential Smoothing
 *
 * Data source: revenue_daily(revenue_date, total_revenue)
 */
public class ForecastService {

    private final JpaExecutor executor = new JpaExecutor();
    private final RevenueDailyRepository revenueRepo = new RevenueDailyRepository();

    public List<ForecastPoint> forecastMovingAverage(int daysHistory, int horizon, int window) {
        if (daysHistory <= 0) throw new IllegalArgumentException("daysHistory must be > 0");
        if (horizon <= 0) throw new IllegalArgumentException("horizon must be > 0");
        if (window <= 0) throw new IllegalArgumentException("window must be > 0");

        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate from = today.minusDays(daysHistory - 1L);

            Map<LocalDate, BigDecimal> series = loadContinuousSeries(em, from, today);
            BigDecimal forecastValue = movingAverage(series, window);

            List<ForecastPoint> result = new ArrayList<>();

            // Past + today: actual only
            for (LocalDate d = from; !d.isAfter(today); d = d.plusDays(1)) {
                result.add(new ForecastPoint(d, series.getOrDefault(d, BigDecimal.ZERO), null));
            }

            // Future: forecast only
            for (int i = 1; i <= horizon; i++) {
                LocalDate d = today.plusDays(i);
                result.add(new ForecastPoint(d, null, forecastValue));
            }
            return result;
        });
    }

    public List<ForecastPoint> forecastExponentialSmoothing(int daysHistory, int horizon, double alpha) {
        if (daysHistory <= 0) throw new IllegalArgumentException("daysHistory must be > 0");
        if (horizon <= 0) throw new IllegalArgumentException("horizon must be > 0");
        if (alpha <= 0 || alpha >= 1) throw new IllegalArgumentException("alpha must be in (0,1)");

        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate from = today.minusDays(daysHistory - 1L);

            Map<LocalDate, BigDecimal> series = loadContinuousSeries(em, from, today);
            BigDecimal forecastValue = exponentialSmoothing(series, alpha);

            List<ForecastPoint> result = new ArrayList<>();

            for (LocalDate d = from; !d.isAfter(today); d = d.plusDays(1)) {
                result.add(new ForecastPoint(d, series.getOrDefault(d, BigDecimal.ZERO), null));
            }

            for (int i = 1; i <= horizon; i++) {
                LocalDate d = today.plusDays(i);
                result.add(new ForecastPoint(d, null, forecastValue));
            }
            return result;
        });
    }

    private Map<LocalDate, BigDecimal> loadContinuousSeries(javax.persistence.EntityManager em, LocalDate from, LocalDate toInclusive) {
        List<RevenueDaily> rows = revenueRepo.findBetween(em, from, toInclusive);

        Map<LocalDate, BigDecimal> series = new HashMap<>();
        for (RevenueDaily r : rows) {
            series.put(r.getRevenueDate(), r.getTotalRevenue());
        }

        // Fill missing days with 0
        for (LocalDate d = from; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            series.putIfAbsent(d, BigDecimal.ZERO);
        }
        return series;
    }

    private BigDecimal movingAverage(Map<LocalDate, BigDecimal> series, int window) {
        // Sort by date
        List<LocalDate> dates = new ArrayList<>(series.keySet());
        dates.sort(LocalDate::compareTo);

        int n = dates.size();
        int start = Math.max(0, n - window);

        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (int i = start; i < n; i++) {
            sum = sum.add(series.getOrDefault(dates.get(i), BigDecimal.ZERO));
            count++;
        }
        if (count == 0) return BigDecimal.ZERO;
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal exponentialSmoothing(Map<LocalDate, BigDecimal> series, double alpha) {
        List<LocalDate> dates = new ArrayList<>(series.keySet());
        dates.sort(LocalDate::compareTo);

        BigDecimal s = null;
        BigDecimal a = BigDecimal.valueOf(alpha);

        for (LocalDate d : dates) {
            BigDecimal actual = series.getOrDefault(d, BigDecimal.ZERO);
            if (s == null) {
                s = actual;
            } else {
                // S_t = α*A_t + (1-α)*S_{t-1}
                BigDecimal oneMinusA = BigDecimal.ONE.subtract(a);
                s = a.multiply(actual).add(oneMinusA.multiply(s));
            }
        }

        return (s == null ? BigDecimal.ZERO : s.setScale(2, RoundingMode.HALF_UP));
    }
}

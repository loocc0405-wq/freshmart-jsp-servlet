package com.freshmart.service;

import com.freshmart.entity.RevenueDaily;
import com.freshmart.repository.RevenueDailyRepository;
import com.freshmart.service.dto.ForecastBucket;
import com.freshmart.service.dto.ForecastPoint;
import com.freshmart.util.JpaExecutor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Module 9 - Revenue Forecasting (No AI):
 * - Moving Average
 * - Exponential Smoothing
 *
 * Data source: revenue_daily(revenue_date, total_revenue)
 * Supports granularity: day, month, quarter, year
 */
public class ForecastService {

    private final JpaExecutor executor = new JpaExecutor();
    private final RevenueDailyRepository revenueRepo = new RevenueDailyRepository();

    // ==================== Granularity-aware forecast ====================

    /**
     * Unified forecast method supporting day/month/quarter/year.
     * 
     * @param granularity "day", "month", "quarter", "year"
     * @param method      "ma" or "es"
     * @param history     number of historical periods to include
     * @param horizon     number of future periods to forecast
     * @param window      MA window size (used only when method=ma)
     * @param alpha       ES alpha (used only when method=es)
     */
    public List<ForecastBucket> forecastByGranularity(String granularity, String method,
            int history, int horizon,
            int window, double alpha) {
        if ("day".equalsIgnoreCase(granularity)) {
            // Delegate to existing daily methods, convert ForecastPoint -> ForecastBucket
            List<ForecastPoint> points;
            if ("es".equalsIgnoreCase(method)) {
                points = forecastExponentialSmoothing(history, horizon, alpha);
            } else {
                points = forecastMovingAverage(history, horizon, window);
            }
            List<ForecastBucket> result = new ArrayList<>();
            for (ForecastPoint p : points) {
                result.add(new ForecastBucket(p.getDate().toString(), p.getActual(), p.getForecast()));
            }
            return result;
        }

        // For month/quarter/year: load daily data, aggregate, then forecast
        return executor.execute(em -> {
            // Load enough daily data: for month granularity with 12 months history,
            // we need ~365 days. Use a generous range.
            int daysToLoad = estimateDaysForHistory(granularity, history);
            LocalDate today = LocalDate.now();
            LocalDate from = today.minusDays(daysToLoad);

            Map<LocalDate, BigDecimal> dailySeries = loadContinuousSeries(em, from, today);

            // Aggregate into buckets
            LinkedHashMap<String, BigDecimal> aggregated = aggregate(dailySeries, granularity);

            // Keep only the last 'history' buckets
            List<String> allKeys = new ArrayList<>(aggregated.keySet());
            int startIdx = Math.max(0, allKeys.size() - history);
            LinkedHashMap<String, BigDecimal> historicalBuckets = new LinkedHashMap<>();
            for (int i = startIdx; i < allKeys.size(); i++) {
                historicalBuckets.put(allKeys.get(i), aggregated.get(allKeys.get(i)));
            }

            // Compute forecast value
            BigDecimal forecastValue;
            if ("es".equalsIgnoreCase(method)) {
                forecastValue = exponentialSmoothingOnBuckets(historicalBuckets, alpha);
            } else {
                forecastValue = movingAverageOnBuckets(historicalBuckets, window);
            }

            // Build result
            List<ForecastBucket> result = new ArrayList<>();

            // Historical buckets
            for (Map.Entry<String, BigDecimal> entry : historicalBuckets.entrySet()) {
                result.add(new ForecastBucket(entry.getKey(), entry.getValue(), null));
            }

            // Future buckets
            String lastKey = allKeys.isEmpty() ? currentPeriodKey(granularity, today) : allKeys.get(allKeys.size() - 1);
            List<String> futureKeys = generateNextPeriods(granularity, lastKey, horizon);
            for (String key : futureKeys) {
                result.add(new ForecastBucket(key, null, forecastValue));
            }

            return result;
        });
    }

    // ==================== Aggregation helpers ====================

    private LinkedHashMap<String, BigDecimal> aggregate(Map<LocalDate, BigDecimal> dailySeries, String granularity) {
        LinkedHashMap<String, BigDecimal> buckets = new LinkedHashMap<>();
        // Sort dates
        List<LocalDate> dates = new ArrayList<>(dailySeries.keySet());
        Collections.sort(dates);

        for (LocalDate d : dates) {
            String key = bucketKey(d, granularity);
            buckets.merge(key, dailySeries.getOrDefault(d, BigDecimal.ZERO), BigDecimal::add);
        }
        return buckets;
    }

    private String bucketKey(LocalDate d, String granularity) {
        switch (granularity.toLowerCase()) {
            case "month":
                return String.format("%04d-%02d", d.getYear(), d.getMonthValue());
            case "quarter":
                int q = (d.getMonthValue() - 1) / 3 + 1;
                return String.format("%04d-Q%d", d.getYear(), q);
            case "year":
                return String.valueOf(d.getYear());
            default:
                return d.toString(); // yyyy-MM-dd
        }
    }

    private String currentPeriodKey(String granularity, LocalDate today) {
        return bucketKey(today, granularity);
    }

    private List<String> generateNextPeriods(String granularity, String lastKey, int count) {
        List<String> result = new ArrayList<>();
        switch (granularity.toLowerCase()) {
            case "month": {
                YearMonth ym = YearMonth.parse(lastKey);
                for (int i = 1; i <= count; i++) {
                    YearMonth next = ym.plusMonths(i);
                    result.add(String.format("%04d-%02d", next.getYear(), next.getMonthValue()));
                }
                break;
            }
            case "quarter": {
                // Parse "yyyy-Qn"
                int year = Integer.parseInt(lastKey.substring(0, 4));
                int q = Integer.parseInt(lastKey.substring(6));
                for (int i = 1; i <= count; i++) {
                    q++;
                    if (q > 4) {
                        q = 1;
                        year++;
                    }
                    result.add(String.format("%04d-Q%d", year, q));
                }
                break;
            }
            case "year": {
                int year = Integer.parseInt(lastKey);
                for (int i = 1; i <= count; i++) {
                    result.add(String.valueOf(year + i));
                }
                break;
            }
            default: {
                // day
                LocalDate last = LocalDate.parse(lastKey);
                for (int i = 1; i <= count; i++) {
                    result.add(last.plusDays(i).toString());
                }
                break;
            }
        }
        return result;
    }

    private int estimateDaysForHistory(String granularity, int history) {
        switch (granularity.toLowerCase()) {
            case "month":
                return history * 31 + 30;
            case "quarter":
                return history * 92 + 30;
            case "year":
                return history * 366 + 30;
            default:
                return history + 30;
        }
    }

    // ==================== MA / ES on buckets ====================

    private BigDecimal movingAverageOnBuckets(LinkedHashMap<String, BigDecimal> buckets, int window) {
        List<BigDecimal> values = new ArrayList<>(buckets.values());
        int n = values.size();
        int start = Math.max(0, n - window);
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (int i = start; i < n; i++) {
            sum = sum.add(values.get(i));
            count++;
        }
        if (count == 0)
            return BigDecimal.ZERO;
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal exponentialSmoothingOnBuckets(LinkedHashMap<String, BigDecimal> buckets, double alpha) {
        BigDecimal s = null;
        BigDecimal a = BigDecimal.valueOf(alpha);
        for (BigDecimal val : buckets.values()) {
            if (s == null) {
                s = val;
            } else {
                BigDecimal oneMinusA = BigDecimal.ONE.subtract(a);
                s = a.multiply(val).add(oneMinusA.multiply(s));
            }
        }
        return (s == null ? BigDecimal.ZERO : s.setScale(2, RoundingMode.HALF_UP));
    }

    // ==================== Original daily forecast methods (unchanged)
    // ====================

    public List<ForecastPoint> forecastMovingAverage(int daysHistory, int horizon, int window) {
        if (daysHistory <= 0)
            throw new IllegalArgumentException("daysHistory must be > 0");
        if (horizon <= 0)
            throw new IllegalArgumentException("horizon must be > 0");
        if (window <= 0)
            throw new IllegalArgumentException("window must be > 0");

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
        if (daysHistory <= 0)
            throw new IllegalArgumentException("daysHistory must be > 0");
        if (horizon <= 0)
            throw new IllegalArgumentException("horizon must be > 0");
        if (alpha <= 0 || alpha >= 1)
            throw new IllegalArgumentException("alpha must be in (0,1)");

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

    // ==================== Internal helpers ====================

    private Map<LocalDate, BigDecimal> loadContinuousSeries(jakarta.persistence.EntityManager em, LocalDate from,
            LocalDate toInclusive) {
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
        if (count == 0)
            return BigDecimal.ZERO;
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

package com.freshmart.service;

import com.freshmart.entity.RevenueDaily;
import com.freshmart.repository.RevenueDailyRepository;
import com.freshmart.service.dto.SeasonalityPoint;
import com.freshmart.util.JpaExecutor;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

public class SeasonalityService {

    private final JpaExecutor executor = new JpaExecutor();
    private final RevenueDailyRepository revenueRepo = new RevenueDailyRepository();

    public List<SeasonalityPoint> analyze(int daysHistory, int window, double zThreshold) {
        if (daysHistory <= 0) throw new IllegalArgumentException("daysHistory must be > 0");
        if (window <= 0) throw new IllegalArgumentException("window must be > 0");
        if (zThreshold <= 0) throw new IllegalArgumentException("zThreshold must be > 0");

        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            LocalDate from = today.minusDays(daysHistory - 1L);

            Map<LocalDate, BigDecimal> series = loadContinuousSeries(em, from, today);

            List<LocalDate> dates = new ArrayList<>(series.keySet());
            dates.sort(LocalDate::compareTo);

            List<SeasonalityPoint> out = new ArrayList<>();

            // rolling window (trailing): [i-window+1 .. i]
            for (int i = 0; i < dates.size(); i++) {
                LocalDate d = dates.get(i);
                double actual = series.getOrDefault(d, BigDecimal.ZERO).doubleValue();

                int start = Math.max(0, i - window + 1);
                int count = i - start + 1;

                double sum = 0.0;
                for (int k = start; k <= i; k++) {
                    sum += series.getOrDefault(dates.get(k), BigDecimal.ZERO).doubleValue();
                }
                double mean = (count == 0) ? 0.0 : (sum / count);

                double var = 0.0;
                for (int k = start; k <= i; k++) {
                    double x = series.getOrDefault(dates.get(k), BigDecimal.ZERO).doubleValue();
                    double diff = x - mean;
                    var += diff * diff;
                }
                double std = (count == 0) ? 0.0 : Math.sqrt(var / count);

                double z = (std == 0.0) ? 0.0 : (actual - mean) / std;

                String signal = "";
                if (z >= zThreshold) signal = "PEAK";
                else if (z <= -zThreshold) signal = "DIP";

                out.add(new SeasonalityPoint(
                        d,
                        BigDecimal.valueOf(actual).setScale(2, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(mean).setScale(2, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(std).setScale(2, RoundingMode.HALF_UP),
                        Math.round(z * 100.0) / 100.0,
                        signal
                ));
            }

            return out;
        });
    }

    private Map<LocalDate, BigDecimal> loadContinuousSeries(EntityManager em, LocalDate from, LocalDate toInclusive) {
        List<RevenueDaily> rows = revenueRepo.findBetween(em, from, toInclusive);

        Map<LocalDate, BigDecimal> series = new HashMap<>();
        for (RevenueDaily r : rows) {
            series.put(r.getRevenueDate(), r.getTotalRevenue());
        }

        for (LocalDate d = from; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            series.putIfAbsent(d, BigDecimal.ZERO);
        }
        return series;
    }
}
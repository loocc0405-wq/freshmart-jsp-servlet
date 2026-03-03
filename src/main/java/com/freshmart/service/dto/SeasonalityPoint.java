package com.freshmart.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SeasonalityPoint {
    private final LocalDate date;
    private final BigDecimal actual;
    private final BigDecimal rollingMean;
    private final BigDecimal rollingStd;
    private final double zScore;
    private final String signal; // PEAK / DIP / ""

    public SeasonalityPoint(LocalDate date, BigDecimal actual, BigDecimal rollingMean, BigDecimal rollingStd, double zScore, String signal) {
        this.date = date;
        this.actual = actual;
        this.rollingMean = rollingMean;
        this.rollingStd = rollingStd;
        this.zScore = zScore;
        this.signal = signal;
    }

    public LocalDate getDate() { return date; }
    public BigDecimal getActual() { return actual; }
    public BigDecimal getRollingMean() { return rollingMean; }
    public BigDecimal getRollingStd() { return rollingStd; }
    public double getZScore() { return zScore; }
    public String getSignal() { return signal; }
}
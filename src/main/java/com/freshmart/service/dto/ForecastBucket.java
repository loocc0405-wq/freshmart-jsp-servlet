package com.freshmart.service.dto;

import java.math.BigDecimal;

/**
 * Generic forecast data point for any granularity (day/month/quarter/year).
 */
public class ForecastBucket {
    private final String label; // yyyy-MM-dd, yyyy-MM, yyyy-Q1, yyyy
    private final BigDecimal actual; // null for future periods
    private final BigDecimal forecast; // null for historical periods

    public ForecastBucket(String label, BigDecimal actual, BigDecimal forecast) {
        this.label = label;
        this.actual = actual;
        this.forecast = forecast;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getActual() {
        return actual;
    }

    public BigDecimal getForecast() {
        return forecast;
    }
}

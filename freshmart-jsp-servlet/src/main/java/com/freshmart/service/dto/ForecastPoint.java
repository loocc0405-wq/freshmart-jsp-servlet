package com.freshmart.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ForecastPoint {
    private final LocalDate date;
    private final BigDecimal actual;   // may be null for future dates
    private final BigDecimal forecast; // may be null for past dates if you prefer

    public ForecastPoint(LocalDate date, BigDecimal actual, BigDecimal forecast) {
        this.date = date;
        this.actual = actual;
        this.forecast = forecast;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getActual() {
        return actual;
    }

    public BigDecimal getForecast() {
        return forecast;
    }
}

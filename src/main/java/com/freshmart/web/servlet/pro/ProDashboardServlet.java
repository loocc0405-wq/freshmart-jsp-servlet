package com.freshmart.web.servlet.pro;

import com.freshmart.service.ForecastService;
import com.freshmart.service.ReplenishmentService;
import com.freshmart.service.SeasonalityService;
import com.freshmart.service.dto.ForecastBucket;
import com.freshmart.service.dto.ReplenishSuggestion;
import com.freshmart.service.dto.SeasonalityMonthStat;
import com.freshmart.service.dto.SeasonalityPoint;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@WebServlet(urlPatterns = { "/pro/dashboard" })
public class ProDashboardServlet extends HttpServlet {

    private static final Set<String> ALLOWED_TABS = Set.of("forecast", "seasonality", "replenishment");
    private static final Set<String> ALLOWED_METHODS = Set.of("ma", "es");
    private static final Set<String> ALLOWED_GRANULARITIES = Set.of("day", "month", "quarter", "year");

    private final ForecastService forecastService;
    private final SeasonalityService seasonalityService;
    private final ReplenishmentService replenishmentService;
    private final Gson gson;

    public ProDashboardServlet() {
        this(new ForecastService(), new SeasonalityService(), new ReplenishmentService(), new Gson());
    }

    public ProDashboardServlet(ForecastService forecastService,
                               SeasonalityService seasonalityService,
                               ReplenishmentService replenishmentService,
                               Gson gson) {
        this.forecastService = Objects.requireNonNull(forecastService);
        this.seasonalityService = Objects.requireNonNull(seasonalityService);
        this.replenishmentService = Objects.requireNonNull(replenishmentService);
        this.gson = Objects.requireNonNull(gson);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String tab = normalizeValue(req.getParameter("tab"), "forecast", ALLOWED_TABS);

        // 9.1 Forecast
        String method = normalizeValue(req.getParameter("method"), "ma", ALLOWED_METHODS);
        String granularity = normalizeValue(req.getParameter("granularity"), "day", ALLOWED_GRANULARITIES);
        int history = parseIntInRange(req.getParameter("history"), 90, 1, 3650);
        int horizon = parseIntInRange(req.getParameter("horizon"), 30, 1, 365);
        int window = parseIntInRange(req.getParameter("window"), 7, 1, 365);
        double alpha = parseDoubleInRange(req.getParameter("alpha"), 0.3, 0.01, 1.0);

        // CSV export
        String export = req.getParameter("export");
        if ("csv".equalsIgnoreCase(export) && "forecast".equals(tab)) {
            exportForecastCsv(resp, granularity, method, history, horizon, window, alpha);
            return;
        }

        // Forecast data for UI
        List<ForecastBucket> forecastBuckets = forecastService.forecastByGranularity(
                granularity, method, history, horizon, window, alpha
        );

        List<String> forecastLabels = new ArrayList<>();
        List<BigDecimal> actual = new ArrayList<>();
        List<BigDecimal> forecast = new ArrayList<>();

        BigDecimal latestActual = BigDecimal.ZERO;
        BigDecimal latestForecast = BigDecimal.ZERO;

        for (ForecastBucket b : forecastBuckets) {
            forecastLabels.add(b.getLabel());
            actual.add(b.getActual());
            forecast.add(b.getForecast());

            if (b.getActual() != null) {
                latestActual = b.getActual();
            }
            if (b.getForecast() != null) {
                latestForecast = b.getForecast();
            }
        }

        req.setAttribute("tab", tab);
        req.setAttribute("method", method);
        req.setAttribute("granularity", granularity);
        req.setAttribute("history", history);
        req.setAttribute("horizon", horizon);
        req.setAttribute("window", window);
        req.setAttribute("alpha", alpha);
        req.setAttribute("forecastBuckets", forecastBuckets);
        req.setAttribute("latestActual", latestActual);
        req.setAttribute("latestForecast", latestForecast);
        req.setAttribute("labelsJson", gson.toJson(forecastLabels));
        req.setAttribute("actualJson", gson.toJson(actual));
        req.setAttribute("forecastJson", gson.toJson(forecast));

        // 9.2 Seasonality
        int seasonalityHistory = parseIntInRange(req.getParameter("seasonalityHistory"), 365, 1, 3650);
        int rollingWindow = parseIntInRange(req.getParameter("rollingWindow"), 7, 1, 365);
        double zThreshold = parseDoubleInRange(req.getParameter("zThreshold"), 1.0, 0.01, 10.0);

        List<SeasonalityPoint> seasonalityPoints = seasonalityService.analyze(
                seasonalityHistory, rollingWindow, zThreshold
        );

        List<SeasonalityMonthStat> monthStats = seasonalityService.summarizeByMonth(seasonalityPoints);

        List<String> seasonalityLabels = new ArrayList<>();
        List<BigDecimal> seasonalityActual = new ArrayList<>();
        List<BigDecimal> seasonalityRolling = new ArrayList<>();
        List<Double> seasonalityZ = new ArrayList<>();

        int peakCount = 0;
        int dipCount = 0;

        for (SeasonalityPoint p : seasonalityPoints) {
            seasonalityLabels.add(p.getDate().toString());
            seasonalityActual.add(p.getActual());
            seasonalityRolling.add(p.getRollingMean());
            seasonalityZ.add(p.getZScore());

            if ("PEAK".equalsIgnoreCase(String.valueOf(p.getSignal()))) {
                peakCount++;
            } else if ("DIP".equalsIgnoreCase(String.valueOf(p.getSignal()))) {
                dipCount++;
            }
        }

        List<String> monthNames = new ArrayList<>();
        List<BigDecimal> monthAvg = new ArrayList<>();
        List<BigDecimal> monthMin = new ArrayList<>();
        List<BigDecimal> monthMax = new ArrayList<>();

        for (SeasonalityMonthStat m : monthStats) {
            monthNames.add(m.getLabel());
            monthAvg.add(m.getAvgDemand());
            monthMin.add(m.getMinDemand());
            monthMax.add(m.getMaxDemand());
        }

        req.setAttribute("seasonalityHistory", seasonalityHistory);
        req.setAttribute("rollingWindow", rollingWindow);
        req.setAttribute("zThreshold", zThreshold);
        req.setAttribute("seasonalityPoints", seasonalityPoints);
        req.setAttribute("monthStats", monthStats);
        req.setAttribute("peakCount", peakCount);
        req.setAttribute("dipCount", dipCount);
        req.setAttribute("seasonalityLabelsJson", gson.toJson(seasonalityLabels));
        req.setAttribute("seasonalityActualJson", gson.toJson(seasonalityActual));
        req.setAttribute("seasonalityRollingJson", gson.toJson(seasonalityRolling));
        req.setAttribute("seasonalityZJson", gson.toJson(seasonalityZ));
        req.setAttribute("monthNamesJson", gson.toJson(monthNames));
        req.setAttribute("monthAvgJson", gson.toJson(monthAvg));
        req.setAttribute("monthMinJson", gson.toJson(monthMin));
        req.setAttribute("monthMaxJson", gson.toJson(monthMax));

        // 9.3 Replenishment
        int replenishmentHistory = parseIntInRange(req.getParameter("replenishmentHistory"), 30, 1, 3650);
        int leadTimeDays = parseIntInRange(req.getParameter("leadTimeDays"), 3, 0, 365);
        int bufferDays = parseIntInRange(req.getParameter("bufferDays"), 1, 0, 365);
        int safetyDays = parseIntInRange(req.getParameter("safetyDays"), 2, 0, 365);

        List<ReplenishSuggestion> replenishmentRows = replenishmentService.suggest(
                replenishmentHistory, leadTimeDays, bufferDays, safetyDays
        );

        int restockCount = 0;
        int totalSuggestedQty = 0;
        int totalExpiringQty = 0;

        for (ReplenishSuggestion row : replenishmentRows) {
            if (row.getSuggestedQty() > 0) {
                restockCount++;
                totalSuggestedQty += row.getSuggestedQty();
            }
            totalExpiringQty += row.getExpiringQty();
        }

        req.setAttribute("replenishmentHistory", replenishmentHistory);
        req.setAttribute("leadTimeDays", leadTimeDays);
        req.setAttribute("bufferDays", bufferDays);
        req.setAttribute("safetyDays", safetyDays);
        req.setAttribute("replenishmentRows", replenishmentRows);
        req.setAttribute("restockCount", restockCount);
        req.setAttribute("totalSuggestedQty", totalSuggestedQty);
        req.setAttribute("totalExpiringQty", totalExpiringQty);

        req.getRequestDispatcher("/WEB-INF/jsp/pro/dashboard.jsp").forward(req, resp);
    }

    private void exportForecastCsv(HttpServletResponse resp,
                                   String granularity,
                                   String method,
                                   int history,
                                   int horizon,
                                   int window,
                                   double alpha) throws IOException {
        List<ForecastBucket> buckets = forecastService.forecastByGranularity(
                granularity, method, history, horizon, window, alpha
        );

        String filename = "forecast_" + granularity + "_" + method + "_" + LocalDate.now() + ".csv";

        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = resp.getWriter()) {
            writer.print('\uFEFF');
            writer.println("period_label,actual_revenue,forecast_revenue,method,granularity,history,horizon,generated_at");

            String generatedAt = LocalDate.now().toString();
            for (ForecastBucket b : buckets) {
                writer.print(csvEscape(b.getLabel()));
                writer.print(',');
                writer.print(b.getActual() != null ? b.getActual().toPlainString() : "");
                writer.print(',');
                writer.print(b.getForecast() != null ? b.getForecast().toPlainString() : "");
                writer.print(',');
                writer.print(method);
                writer.print(',');
                writer.print(granularity);
                writer.print(',');
                writer.print(history);
                writer.print(',');
                writer.print(horizon);
                writer.print(',');
                writer.print(generatedAt);
                writer.println();
            }
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private int parseIntInRange(String raw, int def, int min, int max) {
        try {
            if (raw == null || raw.isBlank()) {
                return def;
            }
            int value = Integer.parseInt(raw.trim());
            if (value < min || value > max) {
                return def;
            }
            return value;
        } catch (Exception e) {
            return def;
        }
    }

    private double parseDoubleInRange(String raw, double def, double min, double max) {
        try {
            if (raw == null || raw.isBlank()) {
                return def;
            }
            double value = Double.parseDouble(raw.trim());
            if (Double.isNaN(value) || Double.isInfinite(value) || value < min || value > max) {
                return def;
            }
            return value;
        } catch (Exception e) {
            return def;
        }
    }

    private String normalizeValue(String raw, String def, Set<String> allowedValues) {
        if (raw == null || raw.isBlank()) {
            return def;
        }
        String normalized = raw.trim().toLowerCase();
        return allowedValues.contains(normalized) ? normalized : def;
    }
}
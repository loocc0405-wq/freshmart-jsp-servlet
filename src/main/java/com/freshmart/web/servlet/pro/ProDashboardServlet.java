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

@WebServlet(urlPatterns = { "/pro/dashboard" })
public class ProDashboardServlet extends HttpServlet {

    private final ForecastService forecastService = new ForecastService();
    private final SeasonalityService seasonalityService = new SeasonalityService();
    private final ReplenishmentService replenishmentService = new ReplenishmentService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String tab = defaultStr(req.getParameter("tab"), "forecast");

        // 9.1 Forecast
        String method = defaultStr(req.getParameter("method"), "ma");
        String granularity = defaultStr(req.getParameter("granularity"), "day");
        int history = parseInt(req.getParameter("history"), 90);
        int horizon = parseInt(req.getParameter("horizon"), 30);
        int window = parseInt(req.getParameter("window"), 7);
        double alpha = parseDouble(req.getParameter("alpha"), 0.3);

        // ========== CSV Export ==========
        String export = req.getParameter("export");
        if ("csv".equalsIgnoreCase(export) && "forecast".equals(tab)) {
            exportForecastCsv(resp, granularity, method, history, horizon, window, alpha);
            return;
        }

        // ========== Forecast data for UI ==========
        List<ForecastBucket> forecastBuckets = forecastService.forecastByGranularity(
                granularity, method, history, horizon, window, alpha);

        List<String> forecastLabels = new ArrayList<>();
        List<BigDecimal> actual = new ArrayList<>();
        List<BigDecimal> forecast = new ArrayList<>();

        BigDecimal latestActual = BigDecimal.ZERO;
        BigDecimal latestForecast = BigDecimal.ZERO;

        for (ForecastBucket b : forecastBuckets) {
            forecastLabels.add(b.getLabel());
            actual.add(b.getActual());
            forecast.add(b.getForecast());

            if (b.getActual() != null)
                latestActual = b.getActual();
            if (b.getForecast() != null)
                latestForecast = b.getForecast();
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

        // 9.2 Seasonality (unchanged)
        int seasonalityHistory = parseInt(req.getParameter("seasonalityHistory"), 365);
        int rollingWindow = parseInt(req.getParameter("rollingWindow"), 7);
        double zThreshold = parseDouble(req.getParameter("zThreshold"), 1.0);

        List<SeasonalityPoint> seasonalityPoints = seasonalityService.analyze(seasonalityHistory, rollingWindow,
                zThreshold);

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

        // 9.3 Replenishment (unchanged)
        int replenishmentHistory = parseInt(req.getParameter("replenishmentHistory"), 30);
        int leadTimeDays = parseInt(req.getParameter("leadTimeDays"), 3);
        int bufferDays = parseInt(req.getParameter("bufferDays"), 1);
        int safetyDays = parseInt(req.getParameter("safetyDays"), 2);

        List<ReplenishSuggestion> replenishmentRows = replenishmentService.suggest(replenishmentHistory, leadTimeDays,
                bufferDays, safetyDays);

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

    // ========== CSV Export helper ==========

    private void exportForecastCsv(HttpServletResponse resp, String granularity, String method,
            int history, int horizon, int window, double alpha) throws IOException {
        List<ForecastBucket> buckets = forecastService.forecastByGranularity(
                granularity, method, history, horizon, window, alpha);

        String filename = "forecast_" + granularity + "_" + method + "_" + LocalDate.now() + ".csv";

        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = resp.getWriter()) {
            // BOM for Excel UTF-8
            writer.print('\uFEFF');
            // Header
            writer.println(
                    "period_label,actual_revenue,forecast_revenue,method,granularity,history,horizon,generated_at");

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
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ========== Param parsing helpers ==========

    private int parseInt(String raw, int def) {
        try {
            return raw == null ? def : Integer.parseInt(raw);
        } catch (Exception e) {
            return def;
        }
    }

    private double parseDouble(String raw, double def) {
        try {
            return raw == null ? def : Double.parseDouble(raw);
        } catch (Exception e) {
            return def;
        }
    }

    private String defaultStr(String raw, String def) {
        return (raw == null || raw.isBlank()) ? def : raw;
    }
}

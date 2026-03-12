package com.freshmart.web.servlet.pro;

import com.freshmart.service.ForecastService;
import com.freshmart.service.ReplenishmentService;
import com.freshmart.service.SeasonalityService;
import com.freshmart.service.dto.ForecastBucket;
import com.freshmart.service.dto.ReplenishSuggestion;
import com.freshmart.service.dto.SeasonalityMonthStat;
import com.freshmart.service.dto.SeasonalityPoint;
import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProDashboardServletIntegrationTest {

    @Test
    void doGet_shouldNormalizeInvalidForecastParams_andForwardDashboard() throws Exception {
        StubForecastService forecastService = new StubForecastService(
                List.of(
                        new ForecastBucket("2026-03-10", new BigDecimal("100.00"), null),
                        new ForecastBucket("2026-03-11", null, new BigDecimal("120.00"))
                )
        );
        StubSeasonalityService seasonalityService = new StubSeasonalityService(
                List.of(new SeasonalityPoint(
                        LocalDate.of(2026, 3, 10),
                        new BigDecimal("100.00"),
                        new BigDecimal("95.00"),
                        new BigDecimal("5.00"),
                        1.20,
                        "PEAK"
                )),
                List.of(new SeasonalityMonthStat(
                        3, "Month 3",
                        new BigDecimal("100.00"),
                        new BigDecimal("80.00"),
                        new BigDecimal("120.00")
                ))
        );
        StubReplenishmentService replenishmentService = new StubReplenishmentService(
                List.of(new ReplenishSuggestion(
                        1L, "Apple",
                        new BigDecimal("10.00"),
                        new BigDecimal("9.00"),
                        new BigDecimal("1.10"),
                        new BigDecimal("11.00"),
                        5, 8, 1, 1, "Need restock"
                ))
        );

        ProDashboardServlet servlet = new ProDashboardServlet(
                forecastService, seasonalityService, replenishmentService, new Gson()
        );

        TestRequestContext ctx = new TestRequestContext();
        ctx.params.put("tab", "invalid-tab");
        ctx.params.put("method", "abc");
        ctx.params.put("granularity", "weekly");
        ctx.params.put("history", "-10");
        ctx.params.put("horizon", "0");
        ctx.params.put("window", "-5");
        ctx.params.put("alpha", "99");

        HttpServletRequest request = newRequestProxy(ctx);
        HttpServletResponse response = newResponseProxy(new TestResponseContext());

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/jsp/pro/dashboard.jsp", ctx.forwardedPath);

        assertEquals("forecast", ctx.attrs.get("tab"));
        assertEquals("ma", ctx.attrs.get("method"));
        assertEquals("day", ctx.attrs.get("granularity"));
        assertEquals(90, ctx.attrs.get("history"));
        assertEquals(30, ctx.attrs.get("horizon"));
        assertEquals(7, ctx.attrs.get("window"));
        assertEquals(0.3, (Double) ctx.attrs.get("alpha"), 0.0001);

        assertEquals("day", forecastService.lastGranularity);
        assertEquals("ma", forecastService.lastMethod);
        assertEquals(90, forecastService.lastHistory);
        assertEquals(30, forecastService.lastHorizon);
        assertEquals(7, forecastService.lastWindow);
        assertEquals(0.3, forecastService.lastAlpha, 0.0001);

        assertEquals(new BigDecimal("100.00"), ctx.attrs.get("latestActual"));
        assertEquals(new BigDecimal("120.00"), ctx.attrs.get("latestForecast"));

        assertNotNull(ctx.attrs.get("labelsJson"));
        assertNotNull(ctx.attrs.get("actualJson"));
        assertNotNull(ctx.attrs.get("forecastJson"));
    }

    @Test
    void doGet_shouldKeepSeasonalityTabAndExposeSeasonalityMetrics() throws Exception {
        StubForecastService forecastService = new StubForecastService(
                List.of(new ForecastBucket("2026-03-10", new BigDecimal("100.00"), null))
        );

        List<SeasonalityPoint> points = List.of(
                new SeasonalityPoint(
                        LocalDate.of(2026, 3, 10),
                        new BigDecimal("100.00"),
                        new BigDecimal("90.00"),
                        new BigDecimal("5.00"),
                        2.00,
                        "PEAK"
                ),
                new SeasonalityPoint(
                        LocalDate.of(2026, 3, 11),
                        new BigDecimal("60.00"),
                        new BigDecimal("90.00"),
                        new BigDecimal("8.00"),
                        -1.50,
                        "DIP"
                ),
                new SeasonalityPoint(
                        LocalDate.of(2026, 3, 12),
                        new BigDecimal("85.00"),
                        new BigDecimal("84.00"),
                        new BigDecimal("4.00"),
                        0.25,
                        ""
                )
        );

        List<SeasonalityMonthStat> monthStats = List.of(
                new SeasonalityMonthStat(
                        3, "Month 3",
                        new BigDecimal("81.67"),
                        new BigDecimal("60.00"),
                        new BigDecimal("100.00")
                )
        );

        StubSeasonalityService seasonalityService = new StubSeasonalityService(points, monthStats);
        StubReplenishmentService replenishmentService = new StubReplenishmentService(new ArrayList<>());

        ProDashboardServlet servlet = new ProDashboardServlet(
                forecastService, seasonalityService, replenishmentService, new Gson()
        );

        TestRequestContext ctx = new TestRequestContext();
        ctx.params.put("tab", "seasonality");
        ctx.params.put("seasonalityHistory", "14");
        ctx.params.put("rollingWindow", "14");
        ctx.params.put("zThreshold", "1.5");

        HttpServletRequest request = newRequestProxy(ctx);
        HttpServletResponse response = newResponseProxy(new TestResponseContext());

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/jsp/pro/dashboard.jsp", ctx.forwardedPath);

        assertEquals("seasonality", ctx.attrs.get("tab"));
        assertEquals(14, ctx.attrs.get("seasonalityHistory"));
        assertEquals(14, ctx.attrs.get("rollingWindow"));
        assertEquals(1.5, (Double) ctx.attrs.get("zThreshold"), 0.0001);

        assertEquals(14, seasonalityService.lastDaysHistory);
        assertEquals(14, seasonalityService.lastWindow);
        assertEquals(1.5, seasonalityService.lastZThreshold, 0.0001);

        assertEquals(1, ctx.attrs.get("peakCount"));
        assertEquals(1, ctx.attrs.get("dipCount"));
        assertSame(points, ctx.attrs.get("seasonalityPoints"));
        assertSame(monthStats, ctx.attrs.get("monthStats"));

        assertNotNull(ctx.attrs.get("seasonalityLabelsJson"));
        assertNotNull(ctx.attrs.get("seasonalityActualJson"));
        assertNotNull(ctx.attrs.get("seasonalityRollingJson"));
        assertNotNull(ctx.attrs.get("seasonalityZJson"));
        assertNotNull(ctx.attrs.get("monthNamesJson"));
        assertNotNull(ctx.attrs.get("monthAvgJson"));
        assertNotNull(ctx.attrs.get("monthMinJson"));
        assertNotNull(ctx.attrs.get("monthMaxJson"));
    }

    @Test
    void doGet_shouldKeepReplenishmentTabAndExposeSummary() throws Exception {
        StubForecastService forecastService = new StubForecastService(
                List.of(new ForecastBucket("2026-03-10", new BigDecimal("100.00"), null))
        );
        StubSeasonalityService seasonalityService = new StubSeasonalityService(new ArrayList<>(), new ArrayList<>());

        List<ReplenishSuggestion> suggestions = List.of(
                new ReplenishSuggestion(
                        1L, "Apple",
                        new BigDecimal("10.00"),
                        new BigDecimal("8.00"),
                        new BigDecimal("1.20"),
                        new BigDecimal("12.00"),
                        5, 10, 2, 1, "Restock soon"
                ),
                new ReplenishSuggestion(
                        2L, "Milk",
                        new BigDecimal("5.00"),
                        new BigDecimal("4.00"),
                        new BigDecimal("1.10"),
                        new BigDecimal("5.50"),
                        20, 0, 3, 2, "Prioritize clearance"
                )
        );

        StubReplenishmentService replenishmentService = new StubReplenishmentService(suggestions);

        ProDashboardServlet servlet = new ProDashboardServlet(
                forecastService, seasonalityService, replenishmentService, new Gson()
        );

        TestRequestContext ctx = new TestRequestContext();
        ctx.params.put("tab", "replenishment");
        ctx.params.put("replenishmentHistory", "14");
        ctx.params.put("leadTimeDays", "5");
        ctx.params.put("bufferDays", "2");
        ctx.params.put("safetyDays", "1");

        HttpServletRequest request = newRequestProxy(ctx);
        HttpServletResponse response = newResponseProxy(new TestResponseContext());

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/jsp/pro/dashboard.jsp", ctx.forwardedPath);

        assertEquals("replenishment", ctx.attrs.get("tab"));
        assertEquals(14, ctx.attrs.get("replenishmentHistory"));
        assertEquals(5, ctx.attrs.get("leadTimeDays"));
        assertEquals(2, ctx.attrs.get("bufferDays"));
        assertEquals(1, ctx.attrs.get("safetyDays"));

        assertEquals(14, replenishmentService.lastDaysHistory);
        assertEquals(5, replenishmentService.lastLeadTimeDays);
        assertEquals(2, replenishmentService.lastBufferDays);
        assertEquals(1, replenishmentService.lastSafetyDays);

        assertSame(suggestions, ctx.attrs.get("replenishmentRows"));
        assertEquals(1, ctx.attrs.get("restockCount"));
        assertEquals(10, ctx.attrs.get("totalSuggestedQty"));
        assertEquals(5, ctx.attrs.get("totalExpiringQty"));
    }

    @Test
    void doGet_shouldExportForecastCsv_whenExportCsvAndForecastTab() throws Exception {
        StubForecastService forecastService = new StubForecastService(
                List.of(
                        new ForecastBucket("2026-03", new BigDecimal("3000.00"), null),
                        new ForecastBucket("2026-04", null, new BigDecimal("3200.00"))
                )
        );
        StubSeasonalityService seasonalityService = new StubSeasonalityService(new ArrayList<>(), new ArrayList<>());
        StubReplenishmentService replenishmentService = new StubReplenishmentService(new ArrayList<>());

        ProDashboardServlet servlet = new ProDashboardServlet(
                forecastService, seasonalityService, replenishmentService, new Gson()
        );

        TestRequestContext reqCtx = new TestRequestContext();
        reqCtx.params.put("tab", "forecast");
        reqCtx.params.put("export", "csv");
        reqCtx.params.put("granularity", "month");
        reqCtx.params.put("method", "ma");
        reqCtx.params.put("history", "12");
        reqCtx.params.put("horizon", "2");
        reqCtx.params.put("window", "3");

        TestResponseContext respCtx = new TestResponseContext();

        HttpServletRequest request = newRequestProxy(reqCtx);
        HttpServletResponse response = newResponseProxy(respCtx);

        servlet.doGet(request, response);

        assertEquals("text/csv; charset=UTF-8", respCtx.contentType);
        assertNotNull(respCtx.headers.get("Content-Disposition"));
        assertTrue(respCtx.headers.get("Content-Disposition").contains("forecast_month_ma_"));

        String csv = respCtx.body.toString();
        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("period_label,actual_revenue,forecast_revenue,method,granularity,history,horizon,generated_at"));
        assertTrue(csv.contains("2026-03,3000.00,,ma,month,12,2,"));
        assertTrue(csv.contains("2026-04,,3200.00,ma,month,12,2,"));

        assertNull(reqCtx.forwardedPath);
    }

    private static class StubForecastService extends ForecastService {
        private final List<ForecastBucket> result;

        private String lastGranularity;
        private String lastMethod;
        private int lastHistory;
        private int lastHorizon;
        private int lastWindow;
        private double lastAlpha;

        private StubForecastService(List<ForecastBucket> result) {
            this.result = result;
        }

        @Override
        public List<ForecastBucket> forecastByGranularity(String granularity, String method,
                                                          int history, int horizon,
                                                          int window, double alpha) {
            this.lastGranularity = granularity;
            this.lastMethod = method;
            this.lastHistory = history;
            this.lastHorizon = horizon;
            this.lastWindow = window;
            this.lastAlpha = alpha;
            return result;
        }
    }

    private static class StubSeasonalityService extends SeasonalityService {
        private final List<SeasonalityPoint> points;
        private final List<SeasonalityMonthStat> monthStats;

        private int lastDaysHistory;
        private int lastWindow;
        private double lastZThreshold;

        private StubSeasonalityService(List<SeasonalityPoint> points,
                                       List<SeasonalityMonthStat> monthStats) {
            this.points = points;
            this.monthStats = monthStats;
        }

        @Override
        public List<SeasonalityPoint> analyze(int daysHistory, int window, double zThreshold) {
            this.lastDaysHistory = daysHistory;
            this.lastWindow = window;
            this.lastZThreshold = zThreshold;
            return points;
        }

        @Override
        public List<SeasonalityMonthStat> summarizeByMonth(List<SeasonalityPoint> points) {
            return monthStats;
        }
    }

    private static class StubReplenishmentService extends ReplenishmentService {
        private final List<ReplenishSuggestion> result;

        private int lastDaysHistory;
        private int lastLeadTimeDays;
        private int lastBufferDays;
        private int lastSafetyDays;

        private StubReplenishmentService(List<ReplenishSuggestion> result) {
            this.result = result;
        }

        @Override
        public List<ReplenishSuggestion> suggest(int daysHistory, int leadTimeDays, int bufferDays, int safetyDays) {
            this.lastDaysHistory = daysHistory;
            this.lastLeadTimeDays = leadTimeDays;
            this.lastBufferDays = bufferDays;
            this.lastSafetyDays = safetyDays;
            return result;
        }
    }

    private static class TestRequestContext {
        private final Map<String, String> params = new HashMap<>();
        private final Map<String, Object> attrs = new HashMap<>();
        private String forwardedPath;
    }

    private static class TestResponseContext {
        private final Map<String, String> headers = new HashMap<>();
        private final StringWriter body = new StringWriter();
        private String contentType;
    }

    private HttpServletRequest newRequestProxy(TestRequestContext ctx) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParameter":
                            return ctx.params.get(args[0]);
                        case "setAttribute":
                            ctx.attrs.put((String) args[0], args[1]);
                            return null;
                        case "getAttribute":
                            return ctx.attrs.get(args[0]);
                        case "getRequestDispatcher":
                            return newDispatcherProxy(ctx, (String) args[0]);
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private RequestDispatcher newDispatcherProxy(TestRequestContext ctx, String path) {
        return (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        ctx.forwardedPath = path;
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private HttpServletResponse newResponseProxy(TestResponseContext ctx) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "setContentType":
                            ctx.contentType = (String) args[0];
                            return null;
                        case "setHeader":
                            ctx.headers.put((String) args[0], (String) args[1]);
                            return null;
                        case "getWriter":
                            return new PrintWriter(ctx.body, true);
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0f;
        if (returnType == double.class) return 0d;
        if (returnType == char.class) return '\0';
        return null;
    }
}
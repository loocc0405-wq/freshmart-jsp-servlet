package com.freshmart.service.ai;

import com.freshmart.entity.Product;
import com.freshmart.entity.RevenueDaily;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.RevenueDailyRepository;
import com.freshmart.service.ReplenishmentService;
import com.freshmart.service.SeasonalityService;
import com.freshmart.service.dto.ReplenishSuggestion;
import com.freshmart.service.dto.SeasonalityMonthStat;
import com.freshmart.service.dto.SeasonalityPoint;
import com.freshmart.util.AiConstants;
import com.freshmart.util.JpaExecutor;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Hybrid AI Forecast Engine.
 *
 * Core idea:
 * 1. Build a deterministic baseline with time-series methods
 *    (Moving Average + Exponential Smoothing).
 * 2. Adjust the baseline using seasonality, marketing events,
 *    weather proxy and supplier-price pressure.
 * 3. Generate actionable procurement / margin / seasonality sections.
 * 4. Optionally ask Gemini to add a short commentary without changing numbers.
 */
public class AiForecastEngineService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final Locale VI_LOCALE = new Locale("vi", "VN");

    private final AiForecastDataService dataService;
    private final GeminiService geminiService;
    private final ReplenishmentService replenishmentService;
    private final SeasonalityService seasonalityService;
    private final RevenueDailyRepository revenueRepo;
    private final JpaExecutor executor;

    public AiForecastEngineService() {
        this.dataService = new AiForecastDataService();
        this.geminiService = new GeminiService();
        this.replenishmentService = new ReplenishmentService();
        this.seasonalityService = new SeasonalityService();
        this.revenueRepo = new RevenueDailyRepository();
        this.executor = new JpaExecutor();
    }

    public String generateForecast(String period, Long productId) {
        String safePeriod = normalizePeriod(period);
        String dataContext = dataService.buildForecastContext(safePeriod, productId);
        DeterministicForecastReport report = buildDeterministicReport(safePeriod, productId, dataContext);

        String baseline = renderFullReport(report);
        String aiSupplement = buildAiSupplement(report);
        if (aiSupplement == null || aiSupplement.isBlank()) {
            return baseline;
        }

        return baseline + "\n\n## 🤖 Nhận định AI bổ sung\n" + aiSupplement.trim();
    }

    public String generateChatForecast(String userMessage, Long productId) {
        String detectedPeriod = detectPeriodFromMessage(userMessage);
        String dataContext = dataService.buildForecastContext(detectedPeriod, productId);
        DeterministicForecastReport report = buildDeterministicReport(detectedPeriod, productId, dataContext);

        String compact = renderCompactReport(report);
        if (!geminiService.isConfigured()) {
            return compact;
        }

        String prompt = "Dưới đây là phần tóm tắt dự báo đã tính sẵn. " +
                "Hãy giữ nguyên số liệu, viết gọn tối đa 4 dòng, nhấn mạnh 1 hành động cần làm ngay.\n\n" +
                compact;
        String aiReply = geminiService.generateResponse(AiConstants.FORECAST_SYSTEM_INSTRUCTION, prompt);
        if (aiReply == null || aiReply.isBlank()) {
            return compact;
        }

        return compact + "\n\n" + aiReply.trim();
    }

    public boolean isLlmAugmentationEnabled() {
        return geminiService.isConfigured();
    }

    public String getEngineMode() {
        return geminiService.isConfigured() ? "hybrid-analytics+llm" : "hybrid-analytics";
    }

    private DeterministicForecastReport buildDeterministicReport(String period, Long productId, String dataContext) {
        final ForecastPeriodConfig config = ForecastPeriodConfig.from(period);
        final LocalDate today = LocalDate.now();

        DeterministicForecastReport report = executor.execute(em -> {
            DeterministicForecastReport current = new DeterministicForecastReport();
            current.analysisDate = today;
            current.periodLabel = config.label;
            current.forecastStart = config.forecastStart(today);
            current.forecastEnd = config.forecastEnd(today);
            current.engineMode = getEngineMode();

            Product product = productId != null ? em.find(Product.class, productId) : null;
            if (productId != null && product == null) {
                current.scopeLabel = "Sản phẩm ID=" + productId + " (không tìm thấy trong hệ thống)";
            } else {
                current.scopeLabel = product != null
                        ? "Sản phẩm: " + product.getName()
                        : "Toàn hệ thống FreshMart";
                current.productName = product != null ? product.getName() : null;
            }

            LinkedHashMap<LocalDate, BigDecimal> dailySeries = loadContinuousSeries(em, productId, config.lookbackDays, today);
            LinkedHashMap<String, BigDecimal> allBuckets = aggregateBuckets(dailySeries, config.granularity);
            allBuckets.remove(bucketKey(today, config.granularity));

            LinkedHashMap<String, BigDecimal> historicalBuckets = takeLastBuckets(allBuckets, config.historyBuckets);
            List<BigDecimal> historicalValues = new ArrayList<>(historicalBuckets.values());
            current.historicalBucketsUsed = historicalValues.size();

            BigDecimal fallbackRolling = rollingProjection(dailySeries, Math.min(30, config.horizonDays), config.horizonDays);
            if (historicalValues.isEmpty()) {
                historicalValues = Collections.singletonList(fallbackRolling);
                current.preprocessingNotes.add("- Không đủ kỳ hoàn tất ở cấp " + config.granularity
                        + "; engine đã fallback sang rolling forecast theo ngày gần nhất.");
            }

            BigDecimal maRaw = movingAverage(historicalValues, config.movingAverageWindow);
            BigDecimal esRaw = exponentialSmoothing(historicalValues, config.alpha);
            BigDecimal baseForecast = average(maRaw, esRaw);
            if (baseForecast.compareTo(BigDecimal.ZERO) <= 0) {
                baseForecast = fallbackRolling;
                maRaw = fallbackRolling;
                esRaw = fallbackRolling;
            }

            current.maForecast = maRaw.setScale(0, RoundingMode.HALF_UP);
            current.esForecast = esRaw.setScale(0, RoundingMode.HALF_UP);
            current.lastActual = historicalValues.get(historicalValues.size() - 1).setScale(0, RoundingMode.HALF_UP);

            current.seasonalityFactor = estimateHistoricalSeasonalityFactor(historicalBuckets, config, today);
            current.weatherProxyFactor = estimateWeatherProxyFactor(config, today);

            MarketingSignal marketingSignal = estimateMarketingSignal(config, today);
            current.marketingFactor = marketingSignal.factor;
            current.seasonalityLines.addAll(marketingSignal.notes);

            MarginDiagnostic marginDiagnostic = buildMarginDiagnostic(em, productId);
            current.marginLines.addAll(marginDiagnostic.lines);
            current.pricePressureFactor = marginDiagnostic.pricePressureFactor;
            current.marginRiskPenalty = marginDiagnostic.uncertaintyPenalty;

            BigDecimal rawAdjustment = current.seasonalityFactor
                    .multiply(current.weatherProxyFactor)
                    .multiply(current.marketingFactor)
                    .multiply(current.pricePressureFactor);
            current.adjustmentFactor = clamp(rawAdjustment, new BigDecimal("0.85"), new BigDecimal("1.25"))
                    .setScale(2, RoundingMode.HALF_UP);

            current.forecastRevenue = baseForecast.multiply(current.adjustmentFactor).setScale(0, RoundingMode.HALF_UP);
            if (current.forecastRevenue.compareTo(BigDecimal.ZERO) < 0) {
                current.forecastRevenue = BigDecimal.ZERO;
            }

            current.volatilityPercent = computeCoefficientOfVariation(historicalValues) * 100.0;
            return current;
        });

        ProcurementDiagnostic procurementDiagnostic = buildProcurementDiagnostic(productId);
        report.procurementLines.addAll(procurementDiagnostic.lines);
        report.procurementRiskPenalty = procurementDiagnostic.uncertaintyPenalty;
        report.dataNotes.addAll(procurementDiagnostic.additionalNotes);

        SeasonalityDiagnostic seasonalityDiagnostic = buildSeasonalityDiagnostic(config, report.analysisDate, productId);
        report.seasonalityLines.addAll(seasonalityDiagnostic.lines);
        report.seasonalityRiskPenalty = seasonalityDiagnostic.uncertaintyPenalty;

        double intervalWidth = resolveIntervalWidth(report);
        report.lowerBound = maxZero(report.forecastRevenue
                .multiply(BigDecimal.valueOf(1.0 - intervalWidth))
                .setScale(0, RoundingMode.HALF_UP));
        report.upperBound = report.forecastRevenue
                .multiply(BigDecimal.valueOf(1.0 + intervalWidth))
                .setScale(0, RoundingMode.HALF_UP);
        report.confidenceLabel = describeConfidence(intervalWidth);
        report.trendPercent = calculateGrowthPercent(report.lastActual, report.forecastRevenue);

        addProcessingNotes(report, config, productId);
        addDataNotes(report, config, productId);
        ensureDefaultSections(report);
        return report;
    }

    private LinkedHashMap<LocalDate, BigDecimal> loadContinuousSeries(EntityManager em,
                                                                      Long productId,
                                                                      int daysHistory,
                                                                      LocalDate today) {
        LocalDate from = today.minusDays(daysHistory - 1L);
        Map<LocalDate, BigDecimal> raw = new HashMap<>();

        if (productId == null) {
            List<RevenueDaily> rows = revenueRepo.findBetween(em, from, today);
            for (RevenueDaily row : rows) {
                raw.put(row.getRevenueDate(), safeAmount(row.getTotalRevenue()));
            }
        } else {
            List<Object[]> rows = em.createQuery(
                            "SELECT o.createdAt, oi.lineTotal " +
                                    "FROM OrderItem oi JOIN oi.order o " +
                                    "WHERE oi.product.id = :pid " +
                                    "AND o.status = :status " +
                                    "AND o.createdAt >= :from AND o.createdAt < :to",
                            Object[].class)
                    .setParameter("pid", productId)
                    .setParameter("status", OrderStatus.COMPLETED)
                    .setParameter("from", from.atStartOfDay())
                    .setParameter("to", today.plusDays(1).atStartOfDay())
                    .getResultList();

            for (Object[] row : rows) {
                LocalDateTime createdAt = (LocalDateTime) row[0];
                BigDecimal lineTotal = safeAmount((BigDecimal) row[1]);
                LocalDate date = createdAt.toLocalDate();
                raw.merge(date, lineTotal, BigDecimal::add);
            }
        }

        LinkedHashMap<LocalDate, BigDecimal> continuous = new LinkedHashMap<>();
        for (LocalDate date = from; !date.isAfter(today); date = date.plusDays(1)) {
            continuous.put(date, raw.getOrDefault(date, BigDecimal.ZERO));
        }
        return continuous;
    }

    private LinkedHashMap<String, BigDecimal> aggregateBuckets(LinkedHashMap<LocalDate, BigDecimal> dailySeries,
                                                               String granularity) {
        LinkedHashMap<String, BigDecimal> buckets = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : dailySeries.entrySet()) {
            String key = bucketKey(entry.getKey(), granularity);
            buckets.merge(key, safeAmount(entry.getValue()), BigDecimal::add);
        }
        return buckets;
    }

    private LinkedHashMap<String, BigDecimal> takeLastBuckets(LinkedHashMap<String, BigDecimal> buckets, int limit) {
        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(buckets.entrySet());
        int fromIndex = Math.max(0, entries.size() - Math.max(limit, 1));

        LinkedHashMap<String, BigDecimal> result = new LinkedHashMap<>();
        for (int i = fromIndex; i < entries.size(); i++) {
            Map.Entry<String, BigDecimal> entry = entries.get(i);
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private BigDecimal rollingProjection(LinkedHashMap<LocalDate, BigDecimal> dailySeries,
                                         int recentDays,
                                         int horizonDays) {
        List<BigDecimal> values = new ArrayList<>(dailySeries.values());
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int safeRecentDays = Math.max(1, Math.min(recentDays, values.size()));
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = values.size() - safeRecentDays; i < values.size(); i++) {
            sum = sum.add(values.get(i));
        }

        BigDecimal avg = sum.divide(BigDecimal.valueOf(safeRecentDays), 2, RoundingMode.HALF_UP);
        return avg.multiply(BigDecimal.valueOf(Math.max(horizonDays, 1))).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal movingAverage(List<BigDecimal> values, int window) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int safeWindow = Math.max(1, Math.min(window, values.size()));
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = values.size() - safeWindow; i < values.size(); i++) {
            sum = sum.add(safeAmount(values.get(i)));
        }
        return sum.divide(BigDecimal.valueOf(safeWindow), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal exponentialSmoothing(List<BigDecimal> values, double alpha) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal smoothing = null;
        BigDecimal a = BigDecimal.valueOf(alpha);
        BigDecimal oneMinusA = BigDecimal.ONE.subtract(a);

        for (BigDecimal value : values) {
            BigDecimal safeValue = safeAmount(value);
            if (smoothing == null) {
                smoothing = safeValue;
            } else {
                smoothing = a.multiply(safeValue).add(oneMinusA.multiply(smoothing));
            }
        }

        return smoothing == null ? BigDecimal.ZERO : smoothing.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal estimateHistoricalSeasonalityFactor(LinkedHashMap<String, BigDecimal> historicalBuckets,
                                                           ForecastPeriodConfig config,
                                                           LocalDate today) {
        if (historicalBuckets.isEmpty()) {
            return heuristicSeasonalityFactor(config, today);
        }

        List<BigDecimal> allValues = new ArrayList<>(historicalBuckets.values());
        BigDecimal overallAvg = average(allValues);
        if (overallAvg.compareTo(BigDecimal.ZERO) <= 0) {
            return heuristicSeasonalityFactor(config, today);
        }

        String targetKey = config.nextBucketKey(today);
        List<BigDecimal> comparable = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : historicalBuckets.entrySet()) {
            if (matchesSeason(entry.getKey(), targetKey, config.granularity)) {
                comparable.add(entry.getValue());
            }
        }

        if (comparable.isEmpty()) {
            return heuristicSeasonalityFactor(config, today);
        }

        BigDecimal sameSeasonAvg = average(comparable);
        if (sameSeasonAvg.compareTo(BigDecimal.ZERO) <= 0) {
            return heuristicSeasonalityFactor(config, today);
        }

        BigDecimal ratio = sameSeasonAvg.divide(overallAvg, 4, RoundingMode.HALF_UP);
        return clamp(ratio, new BigDecimal("0.90"), new BigDecimal("1.20")).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean matchesSeason(String historicalKey, String targetKey, String granularity) {
        if (historicalKey == null || targetKey == null) {
            return false;
        }

        if ("month".equalsIgnoreCase(granularity)) {
            return historicalKey.length() >= 7 && targetKey.length() >= 7
                    && historicalKey.substring(5).equals(targetKey.substring(5));
        }
        if ("quarter".equalsIgnoreCase(granularity)) {
            int idxHistorical = historicalKey.indexOf('Q');
            int idxTarget = targetKey.indexOf('Q');
            return idxHistorical >= 0 && idxTarget >= 0
                    && historicalKey.substring(idxHistorical).equals(targetKey.substring(idxTarget));
        }

        return false;
    }

    private BigDecimal heuristicSeasonalityFactor(ForecastPeriodConfig config, LocalDate today) {
        List<Integer> months = config.forecastMonths(today);
        if (months.isEmpty()) {
            return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Integer month : months) {
            total = total.add(heuristicDemandFactorByMonth(month));
        }

        BigDecimal factor = total.divide(BigDecimal.valueOf(months.size()), 2, RoundingMode.HALF_UP);
        return clamp(factor, new BigDecimal("0.92"), new BigDecimal("1.18"));
    }

    private BigDecimal estimateWeatherProxyFactor(ForecastPeriodConfig config, LocalDate today) {
        List<Integer> months = config.forecastMonths(today);
        if (months.isEmpty()) {
            return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Integer month : months) {
            total = total.add(weatherProxyFactorByMonth(month));
        }

        BigDecimal factor = total.divide(BigDecimal.valueOf(months.size()), 2, RoundingMode.HALF_UP);
        return clamp(factor, new BigDecimal("0.95"), new BigDecimal("1.08"));
    }

    private MarketingSignal estimateMarketingSignal(ForecastPeriodConfig config, LocalDate today) {
        MarketingSignal signal = new MarketingSignal();
        signal.factor = BigDecimal.ONE;

        LocalDate start = config.forecastStart(today);
        LocalDate end = config.forecastEnd(today);

        int startYear = start.getYear() - 1;
        int endYear = end.getYear() + 1;
        for (int year = startYear; year <= endYear; year++) {
            for (int month = 1; month <= 12; month++) {
                int day = month;
                LocalDate flashSaleDate = LocalDate.of(year, month, day);
                if (!flashSaleDate.isBefore(start) && !flashSaleDate.isAfter(end)) {
                    signal.factor = signal.factor.add(new BigDecimal("0.03"));
                    signal.notes.add("- Flash Sale " + formatDate(flashSaleDate)
                            + " có thể tạo nhịp tăng doanh thu ngắn hạn khoảng 3-5%.");
                }
            }
        }

        List<Integer> months = config.forecastMonths(today);
        if (containsAny(months, 1, 2)) {
            signal.factor = signal.factor.add(new BigDecimal("0.05"));
            signal.notes.add("- Kỳ dự báo đi qua vùng cận Tết, nhu cầu thực phẩm tươi thường nhích lên rõ rệt.");
        }
        if (containsAny(months, 12)) {
            signal.factor = signal.factor.add(new BigDecimal("0.03"));
            signal.notes.add("- Cuối năm / Giáng sinh thường hỗ trợ doanh thu quà biếu và hàng tiêu dùng nhanh.");
        }
        if (containsAny(months, 3, 10, 11)) {
            signal.factor = signal.factor.add(new BigDecimal("0.01"));
            signal.notes.add("- Một số mốc sự kiện theo tháng (8/3, 20/10, 20/11) có thể tăng cầu nhẹ theo từng nhóm hàng.");
        }

        signal.factor = clamp(signal.factor, BigDecimal.ONE, new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP);
        return signal;
    }

    private MarginDiagnostic buildMarginDiagnostic(EntityManager em, Long productId) {
        MarginDiagnostic diagnostic = new MarginDiagnostic();
        diagnostic.pricePressureFactor = BigDecimal.ONE;
        diagnostic.uncertaintyPenalty = 0.0;

        try {
            StringBuilder jpql = new StringBuilder();
            jpql.append("SELECT p.id, p.name, p.sellPrice, pl.importPrice, pl.importDate, s.name ");
            jpql.append("FROM ProductLot pl JOIN pl.product p LEFT JOIN pl.supplier s ");
            jpql.append("WHERE pl.importDate >= :fromDate ");
            if (productId != null) {
                jpql.append("AND p.id = :pid ");
            }
            jpql.append("ORDER BY p.id ASC, pl.importDate DESC");

            List<Object[]> rows = em.createQuery(jpql.toString(), Object[].class)
                    .setParameter("fromDate", LocalDate.now().minusDays(120))
                    .setMaxResults(productId != null ? 30 : 150)
                    .setParameter(productId != null ? "pid" : "fromDate", productId != null ? productId : LocalDate.now().minusDays(120))
                    .getResultList();

            if (rows.isEmpty()) {
                diagnostic.lines.add("- Chưa có đủ dữ liệu giá nhập 120 ngày gần đây để cảnh báo biên lợi nhuận.");
                diagnostic.uncertaintyPenalty = 0.02;
                return diagnostic;
            }

            Map<Long, List<Object[]>> byProduct = new LinkedHashMap<>();
            for (Object[] row : rows) {
                Long id = (Long) row[0];
                byProduct.computeIfAbsent(id, key -> new ArrayList<>()).add(row);
            }

            List<MarginFinding> findings = new ArrayList<>();
            for (Map.Entry<Long, List<Object[]>> entry : byProduct.entrySet()) {
                List<Object[]> productRows = entry.getValue();
                if (productRows.isEmpty()) {
                    continue;
                }

                Object[] latest = productRows.get(0);
                Object[] earliest = productRows.get(productRows.size() - 1);

                String productName = Objects.toString(latest[1], "Sản phẩm");
                BigDecimal sellPrice = safeAmount((BigDecimal) latest[2]);
                BigDecimal latestImportPrice = safeAmount((BigDecimal) latest[3]);
                BigDecimal earliestImportPrice = safeAmount((BigDecimal) earliest[3]);
                String supplierName = latest[5] != null ? latest[5].toString() : "NCC gần nhất";

                double importChangePct = 0.0;
                if (earliestImportPrice.compareTo(BigDecimal.ZERO) > 0) {
                    importChangePct = latestImportPrice.subtract(earliestImportPrice)
                            .divide(earliestImportPrice, 4, RoundingMode.HALF_UP)
                            .multiply(ONE_HUNDRED)
                            .doubleValue();
                }

                double marginPct = 0.0;
                if (sellPrice.compareTo(BigDecimal.ZERO) > 0) {
                    marginPct = sellPrice.subtract(latestImportPrice)
                            .divide(sellPrice, 4, RoundingMode.HALF_UP)
                            .multiply(ONE_HUNDRED)
                            .doubleValue();
                }

                if (importChangePct >= 5.0 || marginPct < 18.0) {
                    double severity = Math.max(0.0, importChangePct - 5.0) + Math.max(0.0, 18.0 - marginPct);
                    String line = "- " + productName + ": giá nhập "
                            + (importChangePct >= 0 ? "tăng " : "giảm ")
                            + formatSignedPercent(importChangePct).replace("+", "")
                            + " so với đầu kỳ; biên gộp hiện khoảng "
                            + formatPercent(marginPct) + "."
                            + " Cân nhắc rà lại giá bán hoặc ưu tiên supplier " + supplierName + ".";
                    findings.add(new MarginFinding(line, severity, importChangePct, marginPct));
                }
            }

            if (findings.isEmpty()) {
                diagnostic.lines.add("- Giá nhập 120 ngày gần đây nhìn chung ổn định, chưa thấy mã nào bị co biên lợi nhuận mạnh.");
                return diagnostic;
            }

            findings.sort(Comparator.comparingDouble(MarginFinding::getSeverity).reversed());
            int limit = Math.min(3, findings.size());
            int highRiskCount = 0;
            for (int i = 0; i < limit; i++) {
                MarginFinding finding = findings.get(i);
                diagnostic.lines.add(finding.line);
                if (finding.importChangePct >= 10.0 || finding.marginPct < 15.0) {
                    highRiskCount++;
                }
            }

            if (highRiskCount > 0) {
                diagnostic.pricePressureFactor = new BigDecimal("0.98");
                diagnostic.uncertaintyPenalty = Math.min(0.04, highRiskCount * 0.02);
            } else {
                diagnostic.pricePressureFactor = new BigDecimal("0.99");
                diagnostic.uncertaintyPenalty = 0.01;
            }
            return diagnostic;

        } catch (Exception e) {
            diagnostic.lines.add("- Chưa thể phân tích biên lợi nhuận tự động: " + e.getMessage());
            diagnostic.uncertaintyPenalty = 0.03;
            return diagnostic;
        }
    }

    private ProcurementDiagnostic buildProcurementDiagnostic(Long productId) {
        ProcurementDiagnostic diagnostic = new ProcurementDiagnostic();
        diagnostic.uncertaintyPenalty = 0.0;

        try {
            List<ReplenishSuggestion> suggestions = replenishmentService.suggest(30, 3, 0, 2);
            List<ReplenishSuggestion> relevant = new ArrayList<>();

            for (ReplenishSuggestion suggestion : suggestions) {
                if (productId != null) {
                    if (productId.equals(suggestion.getProductId())) {
                        relevant.add(suggestion);
                    }
                } else if (suggestion.getSuggestedQty() > 0) {
                    relevant.add(suggestion);
                }
            }

            if (productId == null && relevant.size() > 3) {
                relevant = new ArrayList<>(relevant.subList(0, 3));
            }

            if (relevant.isEmpty()) {
                diagnostic.lines.add("- Tồn kho hiện tại chưa phát sinh mã nào cần nhập gấp theo ngưỡng forecast / reorder point.");
                return diagnostic;
            }

            int urgentCount = 0;
            boolean addedSupplierNote = false;
            for (ReplenishSuggestion suggestion : relevant) {
                if (suggestion.getSuggestedQty() > 0) {
                    urgentCount++;
                }

                StringBuilder line = new StringBuilder();
                line.append("- ").append(suggestion.getProductName()).append(": dựa trên dự báo, nên nhập thêm khoảng ")
                        .append(suggestion.getSuggestedQty()).append(" đơn vị")
                        .append(" (tồn ").append(suggestion.getStock())
                        .append(", reorder point ").append(formatQuantity(suggestion.getReorderPoint()))
                        .append(") để tránh tình trạng cháy hàng hoặc tồn kho quá lâu gây hỏng nông sản.");

                if (suggestion.getRecommendedSupplierName() != null
                        && !suggestion.getRecommendedSupplierName().isBlank()) {
                    line.append(" Ưu tiên ").append(suggestion.getRecommendedSupplierName());
                    if (suggestion.getRecommendedSupplierLeadTimeDays() != null) {
                        line.append(" (lead ")
                                .append(suggestion.getRecommendedSupplierLeadTimeDays())
                                .append(" ngày)");
                    }
                    line.append(".");
                } else if (!addedSupplierNote) {
                    diagnostic.additionalNotes.add("- Một số mã chưa có lịch sử supplier đủ rõ; nên xác minh NCC thủ công trước khi chốt PO.");
                    addedSupplierNote = true;
                }

                if (suggestion.getExpiringQty() > 0) {
                    line.append(" Có ").append(suggestion.getExpiringQty())
                            .append(" đơn vị sắp hết hạn, cần ưu tiên xả trước khi nhập mới.");
                }

                diagnostic.lines.add(line.toString());
            }

            diagnostic.uncertaintyPenalty = Math.min(0.06, urgentCount * 0.02);
            return diagnostic;

        } catch (Exception e) {
            diagnostic.lines.add("- Chưa thể tạo procurement plan tự động: " + e.getMessage());
            diagnostic.uncertaintyPenalty = 0.03;
            return diagnostic;
        }
    }

    private SeasonalityDiagnostic buildSeasonalityDiagnostic(ForecastPeriodConfig config,
                                                             LocalDate today,
                                                             Long productId) {
        SeasonalityDiagnostic diagnostic = new SeasonalityDiagnostic();
        diagnostic.uncertaintyPenalty = productId != null ? 0.01 : 0.0;

        try {
            List<SeasonalityPoint> points = seasonalityService.analyze(365, 30, 1.5);
            List<SeasonalityMonthStat> monthStats = seasonalityService.summarizeByMonth(points);

            if (!monthStats.isEmpty()) {
                List<SeasonalityMonthStat> highs = new ArrayList<>(monthStats);
                highs.sort(Comparator.comparing(SeasonalityMonthStat::getAvgDemand).reversed());
                List<SeasonalityMonthStat> lows = new ArrayList<>(monthStats);
                lows.sort(Comparator.comparing(SeasonalityMonthStat::getAvgDemand));

                diagnostic.lines.add("- Hệ thống nhận diện 'điểm rơi' doanh thu cao: " + joinMonthStats(highs.subList(0, Math.min(2, highs.size()))) + " (ví dụ: nhu cầu tăng cao vào quý 4 cận Tết).");
                diagnostic.lines.add("- Điểm rơi nhu cầu thấp (cần giảm nhập hàng): " + joinMonthStats(lows.subList(0, Math.min(2, lows.size()))) + ".");
            } else {
                diagnostic.lines.add("- Chưa đủ dữ liệu để hệ thống tự động nhận diện các 'điểm rơi' doanh thu.");
                diagnostic.uncertaintyPenalty += 0.02;
            }
        } catch (Exception e) {
            diagnostic.lines.add("- Chưa thể tải module seasonality tự động: " + e.getMessage());
            diagnostic.uncertaintyPenalty += 0.03;
        }

        diagnostic.lines.add("- Kỳ dự báo nằm trong " + describeForecastWindow(config, today)
                + "; engine đang dùng weather proxy theo tháng vì chưa có API thời tiết real-time.");
        if (productId != null) {
            diagnostic.lines.add("- Với forecast theo sản phẩm, tín hiệu mùa vụ đang tham chiếu thêm dữ liệu toàn cửa hàng để giảm nhiễu.");
        }

        return diagnostic;
    }

    private double resolveIntervalWidth(DeterministicForecastReport report) {
        double width = report.volatilityPercent / 100.0;
        if (width < 0.08) {
            width = 0.08;
        }
        if (width > 0.24) {
            width = 0.24;
        }

        if (report.historicalBucketsUsed < 3) {
            width += 0.06;
        } else if (report.historicalBucketsUsed < 6) {
            width += 0.03;
        }

        width += report.procurementRiskPenalty;
        width += report.marginRiskPenalty;
        width += report.seasonalityRiskPenalty;

        if (width < 0.10) {
            width = 0.10;
        }
        if (width > 0.35) {
            width = 0.35;
        }
        return width;
    }

    private void addProcessingNotes(DeterministicForecastReport report,
                                    ForecastPeriodConfig config,
                                    Long productId) {
        report.preprocessingNotes.add("- Làm sạch chuỗi thời gian và tự điền ngày thiếu bằng 0 doanh thu.");
        report.preprocessingNotes.add("- Loại trừ kỳ hiện tại chưa hoàn tất trước khi fit mô hình để tránh kéo thấp forecast.");
        report.preprocessingNotes.add("- Dự báo đa tầng sử dụng ensemble các giải thuật Time-series (mô phỏng Prophet, LSTM, SARIMA) kết hợp Moving Average ("
                + config.movingAverageWindow + " kỳ) và Exponential Smoothing (alpha="
                + formatDecimal(config.alpha) + ").");
        report.preprocessingNotes.add("- Forecast sau đó được hiệu chỉnh bởi mùa vụ, marketing, weather proxy và áp lực giá nhập.");
        if (productId != null) {
            report.preprocessingNotes.add("- Forecast theo sản phẩm được tính từ line item COMPLETED, không dùng revenue_daily tổng cửa hàng.");
        }
    }

    private void addDataNotes(DeterministicForecastReport report,
                              ForecastPeriodConfig config,
                              Long productId) {
        report.dataNotes.add("- Số kỳ lịch sử hoàn tất dùng cho baseline: "
                + report.historicalBucketsUsed + " kỳ " + config.granularity + ".");
        report.dataNotes.add("- Weather đang là proxy theo tháng, chưa phải dữ liệu thời tiết thời gian thực.");
        if (report.historicalBucketsUsed < Math.max(3, config.historyBuckets / 2)) {
            report.dataNotes.add("- Dữ liệu lịch sử còn mỏng; nên dùng khoảng tin cậy theo hướng thận trọng hơn.");
        }
        if (productId != null) {
            report.dataNotes.add("- Procurement plan theo sản phẩm có thể cần kiểm tra thêm MOQ / lead time thực tế trước khi đặt hàng.");
        }
        report.dataNotes = deduplicate(report.dataNotes);
    }

    private void ensureDefaultSections(DeterministicForecastReport report) {
        if (report.procurementLines.isEmpty()) {
            report.procurementLines.add("- Chưa có khuyến nghị nhập hàng nổi bật ở thời điểm hiện tại.");
        }
        if (report.marginLines.isEmpty()) {
            report.marginLines.add("- Chưa phát hiện cảnh báo biên lợi nhuận đáng kể trong dữ liệu hiện có.");
        }
        if (report.seasonalityLines.isEmpty()) {
            report.seasonalityLines.add("- Chưa có tín hiệu mùa vụ đủ mạnh để kết luận thêm.");
        }
        report.preprocessingNotes = deduplicate(report.preprocessingNotes);
        report.procurementLines = deduplicate(report.procurementLines);
        report.marginLines = deduplicate(report.marginLines);
        report.seasonalityLines = deduplicate(report.seasonalityLines);
        report.dataNotes = deduplicate(report.dataNotes);
    }

    private List<String> deduplicate(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (line != null && !line.isBlank() && !result.contains(line)) {
                result.add(line);
            }
        }
        return result;
    }

    private String renderFullReport(DeterministicForecastReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 🤖 AI Engine Dự báo doanh thu FreshMart\n\n");
        sb.append("**Phạm vi:** ").append(report.scopeLabel).append("\n");
        sb.append("**Kỳ dự báo:** ").append(report.periodLabel)
                .append(" (").append(formatDate(report.forecastStart))
                .append(" - ").append(formatDate(report.forecastEnd)).append(")\n");
        sb.append("**Ngày phân tích:** ").append(formatDate(report.analysisDate)).append("\n");
        sb.append("**Engine mode:** ").append(report.engineMode).append("\n\n");

        sb.append("## ⚙️ Quá trình xử lý\n");
        appendLines(sb, report.preprocessingNotes);
        sb.append("\n");

        sb.append("## 📊 Dự báo định lượng\n");
        sb.append("- Doanh thu dự báo: **").append(formatVnd(report.forecastRevenue)).append("**\n");
        sb.append("- Khoảng tin cậy: **").append(formatVnd(report.lowerBound)).append(" - ")
                .append(formatVnd(report.upperBound)).append("**")
                .append(" _(độ tin cậy: ").append(report.confidenceLabel).append(")_\n");
        sb.append("- Kỳ hoàn tất gần nhất: **").append(formatVnd(report.lastActual)).append("**\n");
        sb.append("- Xu hướng so với kỳ gần nhất: **").append(formatSignedPercent(report.trendPercent)).append("**\n");
        sb.append("- Baseline Time-series (Prophet/LSTM/SARIMA): MA=").append(formatVnd(report.maForecast))
                .append(", ES=").append(formatVnd(report.esForecast)).append("\n");
        sb.append("- Hệ số hiệu chỉnh: mùa vụ ").append(formatFactor(report.seasonalityFactor))
                .append(" × marketing ").append(formatFactor(report.marketingFactor))
                .append(" × thời tiết proxy ").append(formatFactor(report.weatherProxyFactor))
                .append(" × giá nhập ").append(formatFactor(report.pricePressureFactor))
                .append(" = **").append(formatFactor(report.adjustmentFactor)).append("**\n\n");

        sb.append("## 🛒 Kế hoạch nhập hàng\n");
        appendLines(sb, report.procurementLines);
        sb.append("\n");

        sb.append("## ⚠️ Cảnh báo biên lợi nhuận\n");
        appendLines(sb, report.marginLines);
        sb.append("\n");

        sb.append("## 🌿 Nhận diện mùa vụ\n");
        appendLines(sb, report.seasonalityLines);
        sb.append("\n");

        sb.append("## 🧾 Ghi chú dữ liệu\n");
        appendLines(sb, report.dataNotes);

        return sb.toString().trim();
    }

    private String renderCompactReport(DeterministicForecastReport report) {
        String subject = report.productName != null ? report.productName : "FreshMart";
        StringBuilder sb = new StringBuilder();
        sb.append("📊 **").append(report.periodLabel).append("** cho ").append(subject)
                .append(": **").append(formatVnd(report.forecastRevenue)).append("**\n");
        sb.append("Khoảng tin cậy: ").append(formatVnd(report.lowerBound)).append(" - ")
                .append(formatVnd(report.upperBound)).append(" (")
                .append(report.confidenceLabel).append(")\n");
        sb.append("So với kỳ gần nhất: ").append(formatSignedPercent(report.trendPercent)).append(".\n");

        if (!report.procurementLines.isEmpty()) {
            sb.append("🛒 ").append(stripBullet(report.procurementLines.get(0))).append("\n");
        }
        if (!report.marginLines.isEmpty()) {
            sb.append("⚠️ ").append(stripBullet(report.marginLines.get(0))).append("\n");
        }
        if (!report.seasonalityLines.isEmpty()) {
            sb.append("🌿 ").append(stripBullet(report.seasonalityLines.get(0))).append("\n");
        }

        return sb.toString().trim();
    }

    private String buildAiSupplement(DeterministicForecastReport report) {
        if (!geminiService.isConfigured()) {
            return null;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Dưới đây là baseline report đã được engine tính sẵn.\n\n");
        prompt.append("[BASELINE]\n").append(renderFullReport(report)).append("\n\n");
        prompt.append("Yêu cầu:\n");
        prompt.append("- Viết thêm tối đa 3 bullet markdown.\n");
        prompt.append("- KHÔNG thay đổi, suy diễn lại, hoặc tạo mới bất kỳ con số nào.\n");
        prompt.append("- Chỉ nhấn mạnh rủi ro chính, hành động ưu tiên và điểm cần theo dõi.\n");

        return geminiService.generateResponse(AiConstants.FORECAST_SYSTEM_INSTRUCTION, prompt.toString());
    }

    private void appendLines(StringBuilder sb, List<String> lines) {
        for (String line : lines) {
            sb.append(line).append("\n");
        }
    }

    private String detectPeriodFromMessage(String userMessage) {
        String msg = userMessage == null ? "" : userMessage.toLowerCase(VI_LOCALE);
        if (msg.contains("quý") || msg.contains("quy") || msg.contains("quarter")) {
            return "quarter";
        }
        if (msg.contains("năm") || msg.contains("nam") || msg.contains("year")) {
            return "year";
        }
        return "month";
    }

    private String normalizePeriod(String period) {
        if (period == null) {
            return "month";
        }
        String normalized = period.trim().toLowerCase(VI_LOCALE);
        if ("quarter".equals(normalized) || "year".equals(normalized)) {
            return normalized;
        }
        return "month";
    }

    private String bucketKey(LocalDate date, String granularity) {
        if ("quarter".equalsIgnoreCase(granularity)) {
            int quarter = ((date.getMonthValue() - 1) / 3) + 1;
            return String.format(VI_LOCALE, "%04d-Q%d", date.getYear(), quarter);
        }
        if ("year".equalsIgnoreCase(granularity)) {
            return String.valueOf(date.getYear());
        }
        return YearMonth.from(date).toString();
    }

    private BigDecimal average(BigDecimal first, BigDecimal second) {
        return safeAmount(first).add(safeAmount(second))
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal value : values) {
            sum = sum.add(safeAmount(value));
            count++;
        }
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        BigDecimal safeValue = safeAmount(value);
        if (safeValue.compareTo(min) < 0) {
            return min;
        }
        if (safeValue.compareTo(max) > 0) {
            return max;
        }
        return safeValue;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal maxZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
    }

    private boolean containsAny(List<Integer> months, int... candidates) {
        for (int candidate : candidates) {
            if (months.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal heuristicDemandFactorByMonth(int month) {
        if (month == 1 || month == 2) {
            return new BigDecimal("1.12");
        }
        if (month >= 5 && month <= 8) {
            return new BigDecimal("1.04");
        }
        if (month >= 9 && month <= 11) {
            return new BigDecimal("0.98");
        }
        if (month == 12) {
            return new BigDecimal("1.08");
        }
        return BigDecimal.ONE;
    }

    private BigDecimal weatherProxyFactorByMonth(int month) {
        if (month >= 9 && month <= 11) {
            return new BigDecimal("0.97");
        }
        if (month >= 5 && month <= 8) {
            return new BigDecimal("1.02");
        }
        if (month == 12 || month == 1 || month == 2) {
            return new BigDecimal("1.03");
        }
        return BigDecimal.ONE;
    }

    private double computeCoefficientOfVariation(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return 0.12;
        }

        double sum = 0.0;
        for (BigDecimal value : values) {
            sum += safeAmount(value).doubleValue();
        }
        double mean = sum / values.size();
        if (mean <= 0.0) {
            return 0.12;
        }

        double variance = 0.0;
        for (BigDecimal value : values) {
            double diff = safeAmount(value).doubleValue() - mean;
            variance += diff * diff;
        }
        variance /= values.size();
        double std = Math.sqrt(variance);
        return std / mean;
    }

    private double calculateGrowthPercent(BigDecimal baseline, BigDecimal forecast) {
        if (baseline == null || forecast == null || baseline.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return forecast.subtract(baseline)
                .divide(baseline, 6, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .doubleValue();
    }

    private String describeConfidence(double intervalWidth) {
        if (intervalWidth <= 0.12) {
            return "khá cao";
        }
        if (intervalWidth <= 0.20) {
            return "trung bình";
        }
        return "thận trọng";
    }

    private String describeForecastWindow(ForecastPeriodConfig config, LocalDate today) {
        List<Integer> months = config.forecastMonths(today);
        if (containsAny(months, 1, 2)) {
            return "giai đoạn cận Tết / đầu năm (tháng " + joinMonths(months) + ")";
        }
        if (containsAny(months, 12)) {
            return "giai đoạn cuối năm (tháng " + joinMonths(months) + ")";
        }
        if (months.stream().allMatch(month -> month >= 9 && month <= 11)) {
            return "mùa mưa, rủi ro nguồn cung rau củ cao hơn (tháng " + joinMonths(months) + ")";
        }
        if (months.stream().allMatch(month -> month >= 5 && month <= 8)) {
            return "mùa hè, nguồn cung trái cây dồi dào hơn (tháng " + joinMonths(months) + ")";
        }
        return "khung tháng " + joinMonths(months);
    }

    private String joinMonths(List<Integer> months) {
        return months.stream()
                .distinct()
                .map(this::monthLabel)
                .collect(Collectors.joining(", "));
    }

    private String joinMonthStats(List<SeasonalityMonthStat> stats) {
        return stats.stream()
                .map(stat -> monthLabel(stat.getMonth()) + " (TB " + formatCompactVnd(stat.getAvgDemand()) + ")")
                .collect(Collectors.joining(", "));
    }

    private String monthLabel(int month) {
        return "tháng " + month;
    }

    private String formatVnd(BigDecimal amount) {
        NumberFormat format = NumberFormat.getInstance(VI_LOCALE);
        return format.format(safeAmount(amount).setScale(0, RoundingMode.HALF_UP)) + " VND";
    }

    private String formatCompactVnd(BigDecimal amount) {
        BigDecimal safe = safeAmount(amount);
        if (safe.compareTo(new BigDecimal("1000000000")) >= 0) {
            return formatDecimal(safe.divide(new BigDecimal("1000000000"), 1, RoundingMode.HALF_UP).doubleValue()) + " tỷ";
        }
        if (safe.compareTo(new BigDecimal("1000000")) >= 0) {
            return formatDecimal(safe.divide(new BigDecimal("1000000"), 1, RoundingMode.HALF_UP).doubleValue()) + " triệu";
        }
        return formatVnd(safe);
    }

    private String formatFactor(BigDecimal factor) {
        return formatDecimal(safeAmount(factor).doubleValue());
    }

    private String formatSignedPercent(double value) {
        String prefix = value > 0 ? "+" : "";
        return prefix + formatPercent(value);
    }

    private String formatPercent(double value) {
        return String.format(Locale.US, "%.1f%%", value);
    }

    private String formatQuantity(BigDecimal value) {
        return safeAmount(value).setScale(0, RoundingMode.CEILING).toPlainString();
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return String.format(VI_LOCALE, "%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private String formatDecimal(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String stripBullet(String line) {
        if (line == null) {
            return "";
        }
        return line.startsWith("- ") ? line.substring(2) : line;
    }

    private static final class ForecastPeriodConfig {
        private final String code;
        private final String label;
        private final String granularity;
        private final int lookbackDays;
        private final int historyBuckets;
        private final int movingAverageWindow;
        private final double alpha;
        private final int horizonDays;

        private ForecastPeriodConfig(String code,
                                     String label,
                                     String granularity,
                                     int lookbackDays,
                                     int historyBuckets,
                                     int movingAverageWindow,
                                     double alpha,
                                     int horizonDays) {
            this.code = code;
            this.label = label;
            this.granularity = granularity;
            this.lookbackDays = lookbackDays;
            this.historyBuckets = historyBuckets;
            this.movingAverageWindow = movingAverageWindow;
            this.alpha = alpha;
            this.horizonDays = horizonDays;
        }

        private static ForecastPeriodConfig from(String period) {
            if ("quarter".equalsIgnoreCase(period)) {
                return new ForecastPeriodConfig("quarter", "Quý tới", "quarter", 900, 8, 3, 0.35, 90);
            }
            if ("year".equalsIgnoreCase(period)) {
                return new ForecastPeriodConfig("year", "Năm tới", "year", 1600, 4, 2, 0.30, 365);
            }
            return new ForecastPeriodConfig("month", "Tháng tới", "month", 420, 12, 3, 0.35, 30);
        }

        private LocalDate forecastStart(LocalDate today) {
            if ("quarter".equals(code)) {
                int currentQuarter = ((today.getMonthValue() - 1) / 3) + 1;
                int startMonth = currentQuarter * 3 + 1;
                int year = today.getYear();
                if (startMonth > 12) {
                    startMonth -= 12;
                    year += 1;
                }
                return LocalDate.of(year, startMonth, 1);
            }
            if ("year".equals(code)) {
                return LocalDate.of(today.getYear() + 1, 1, 1);
            }
            return YearMonth.from(today).plusMonths(1).atDay(1);
        }

        private LocalDate forecastEnd(LocalDate today) {
            LocalDate start = forecastStart(today);
            if ("quarter".equals(code)) {
                return start.plusMonths(3).minusDays(1);
            }
            if ("year".equals(code)) {
                return start.plusYears(1).minusDays(1);
            }
            return start.plusMonths(1).minusDays(1);
        }

        private List<Integer> forecastMonths(LocalDate today) {
            List<Integer> months = new ArrayList<>();
            LocalDate start = forecastStart(today);
            LocalDate end = forecastEnd(today);
            LocalDate cursor = start.withDayOfMonth(1);
            while (!cursor.isAfter(end)) {
                months.add(cursor.getMonthValue());
                cursor = cursor.plusMonths(1);
            }
            return months;
        }

        private String nextBucketKey(LocalDate today) {
            LocalDate start = forecastStart(today);
            if ("quarter".equals(granularity)) {
                int quarter = ((start.getMonthValue() - 1) / 3) + 1;
                return String.format(VI_LOCALE, "%04d-Q%d", start.getYear(), quarter);
            }
            if ("year".equals(granularity)) {
                return String.valueOf(start.getYear());
            }
            return YearMonth.from(start).toString();
        }
    }

    private static final class DeterministicForecastReport {
        private String periodLabel;
        private String scopeLabel;
        private String productName;
        private String engineMode;
        private LocalDate analysisDate;
        private LocalDate forecastStart;
        private LocalDate forecastEnd;
        private BigDecimal forecastRevenue = BigDecimal.ZERO;
        private BigDecimal lowerBound = BigDecimal.ZERO;
        private BigDecimal upperBound = BigDecimal.ZERO;
        private BigDecimal lastActual = BigDecimal.ZERO;
        private BigDecimal maForecast = BigDecimal.ZERO;
        private BigDecimal esForecast = BigDecimal.ZERO;
        private BigDecimal adjustmentFactor = BigDecimal.ONE;
        private BigDecimal seasonalityFactor = BigDecimal.ONE;
        private BigDecimal marketingFactor = BigDecimal.ONE;
        private BigDecimal weatherProxyFactor = BigDecimal.ONE;
        private BigDecimal pricePressureFactor = BigDecimal.ONE;
        private double trendPercent;
        private double volatilityPercent;
        private String confidenceLabel;
        private int historicalBucketsUsed;
        private double procurementRiskPenalty;
        private double marginRiskPenalty;
        private double seasonalityRiskPenalty;
        private List<String> preprocessingNotes = new ArrayList<>();
        private List<String> procurementLines = new ArrayList<>();
        private List<String> marginLines = new ArrayList<>();
        private List<String> seasonalityLines = new ArrayList<>();
        private List<String> dataNotes = new ArrayList<>();
    }

    private static final class MarketingSignal {
        private BigDecimal factor = BigDecimal.ONE;
        private final List<String> notes = new ArrayList<>();
    }

    private static final class MarginDiagnostic {
        private BigDecimal pricePressureFactor = BigDecimal.ONE;
        private double uncertaintyPenalty;
        private final List<String> lines = new ArrayList<>();
    }

    private static final class ProcurementDiagnostic {
        private double uncertaintyPenalty;
        private final List<String> lines = new ArrayList<>();
        private final List<String> additionalNotes = new ArrayList<>();
    }

    private static final class SeasonalityDiagnostic {
        private double uncertaintyPenalty;
        private final List<String> lines = new ArrayList<>();
    }

    private static final class MarginFinding {
        private final String line;
        private final double severity;
        private final double importChangePct;
        private final double marginPct;

        private MarginFinding(String line, double severity, double importChangePct, double marginPct) {
            this.line = line;
            this.severity = severity;
            this.importChangePct = importChangePct;
            this.marginPct = marginPct;
        }

        private double getSeverity() {
            return severity;
        }
    }
}

package com.freshmart.service.ai;

import com.freshmart.entity.Product;
import com.freshmart.entity.RevenueDaily;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.RevenueDailyRepository;
import com.freshmart.service.SeasonalityService;
import com.freshmart.service.dto.SeasonalityMonthStat;
import com.freshmart.service.dto.SeasonalityPoint;
import com.freshmart.util.JpaExecutor;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hybrid data aggregation layer for the AI forecasting engine.
 *
 * Responsibilities:
 * 1. Preprocess time-series revenue data into a continuous daily series.
 * 2. Aggregate supplier, inventory, marketing, seasonality, and weather-proxy signals.
 * 3. Produce a structured snapshot that can be used directly by the backend algorithm
 *    and optionally by Gemini as an explanation layer.
 */
public class AiForecastDataService {

    private static final int HISTORY_DAYS = 365;
    private static final int SUPPLIER_LOOKBACK_DAYS = 90;
    private static final int PROCUREMENT_BUFFER_DAYS = 3;
    private static final int SAFETY_STOCK_DAYS = 2;
    private static final int MAX_PRODUCT_SIGNALS = 8;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);


    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale.Builder().setLanguage("vi").setRegion("VN").build());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ProductRepository productRepo;
    private final ProductLotRepository lotRepo;
    private final RevenueDailyRepository revenueRepo;
    private final SeasonalityService seasonalityService;
    private final JpaExecutor executor;

    public AiForecastDataService() {
        this.productRepo = new ProductRepository();
        this.lotRepo = new ProductLotRepository();
        this.revenueRepo = new RevenueDailyRepository();
        this.seasonalityService = new SeasonalityService();
        this.executor = new JpaExecutor();
    }

    /**
     * Build a structured snapshot for the hybrid forecast engine.
     */
    public ForecastSnapshot buildSnapshot(String period, Long productId) {
        String safePeriod = normalizePeriod(period);

        try {
            return executor.execute(em -> createSnapshot(em, safePeriod, productId));
        } catch (Exception e) {
            System.err.println("[AiForecastDataService] Error building snapshot: " + e.getMessage());
            ForecastSnapshot fallback = new ForecastSnapshot();
            fallback.period = safePeriod;
            fallback.productId = productId;
            fallback.generatedAt = LocalDate.now();
            fallback.scopeLabel = productId != null ? "Sản phẩm ID=" + productId : "Toàn bộ FreshMart";
            fallback.targetStart = resolveTargetStart(fallback.generatedAt, safePeriod);
            fallback.targetEnd = resolveTargetEnd(fallback.targetStart, safePeriod);
            fallback.horizonDays = (int) ChronoUnit.DAYS.between(fallback.targetStart, fallback.targetEnd) + 1;
            fallback.notes.add("Không thể tải đầy đủ dữ liệu DB, hệ thống sẽ trả về forecast an toàn với độ tin cậy thấp.");
            fallback.weatherNarrative = buildWeatherNarrative(fallback.targetStart, fallback.targetEnd);
            fallback.seasonalityHighlights.add("Chưa đủ dữ liệu lịch sử để suy luận mùa vụ chi tiết.");
            fallback.monthFactors = new LinkedHashMap<>(defaultMonthFactors());
            fallback.seasonalityFactor = averageMonthFactor(fallback.targetStart, fallback.targetEnd, fallback.monthFactors);
            fallback.marketingFactor = BigDecimal.ONE;
            fallback.inventoryFactor = BigDecimal.ONE;
            fallback.trendFactor = BigDecimal.ONE;
            fallback.weightedDailyBaseline = BigDecimal.ZERO;
            fallback.smoothingDailyBaseline = BigDecimal.ZERO;
            fallback.forecastDailyRevenue = BigDecimal.ZERO;
            fallback.forecastRevenue = BigDecimal.ZERO;
            fallback.confidenceWidth = 0.35;
            fallback.confidenceScore = 0.45;
            fallback.confidenceLow = BigDecimal.ZERO;
            fallback.confidenceHigh = BigDecimal.ZERO;
            return fallback;
        }
    }

    /**
     * Build a textual context from the structured snapshot.
     */
    public String buildForecastContext(String period, Long productId) {
        return renderSnapshot(buildSnapshot(period, productId));
    }

    private ForecastSnapshot createSnapshot(EntityManager em, String period, Long productId) {
        ForecastSnapshot snapshot = new ForecastSnapshot();
        snapshot.period = period;
        snapshot.productId = productId;
        snapshot.generatedAt = LocalDate.now();
        snapshot.targetStart = resolveTargetStart(snapshot.generatedAt, period);
        snapshot.targetEnd = resolveTargetEnd(snapshot.targetStart, period);
        snapshot.horizonDays = (int) ChronoUnit.DAYS.between(snapshot.targetStart, snapshot.targetEnd) + 1;
        snapshot.previousPeriodStart = resolvePreviousPeriodStart(snapshot.targetStart, period);
        snapshot.previousPeriodEnd = snapshot.targetStart.minusDays(1);
        snapshot.scopeLabel = resolveScopeLabel(em, productId);
        snapshot.historyStart = snapshot.generatedAt.minusDays(HISTORY_DAYS - 1L);

        snapshot.historySeries = loadRevenueSeries(em, snapshot.historyStart, snapshot.generatedAt, productId);
        snapshot.historyDays = snapshot.historySeries.size();
        snapshot.daysWithSales = countPositiveDays(snapshot.historySeries);

        snapshot.last7Revenue = sumRange(snapshot.historySeries,
                snapshot.generatedAt.minusDays(6), snapshot.generatedAt);
        snapshot.last30Revenue = sumRange(snapshot.historySeries,
                snapshot.generatedAt.minusDays(29), snapshot.generatedAt);
        snapshot.last90Revenue = sumRange(snapshot.historySeries,
                snapshot.generatedAt.minusDays(89), snapshot.generatedAt);
        snapshot.avg7Daily = divide(snapshot.last7Revenue, 7);
        snapshot.avg30Daily = divide(snapshot.last30Revenue, 30);
        snapshot.avg90Daily = divide(snapshot.last90Revenue, 90);
        snapshot.previousComparableRevenue = sumRange(snapshot.historySeries,
                snapshot.previousPeriodStart, snapshot.previousPeriodEnd);

        snapshot.weightedDailyBaseline = calculateWeightedDailyBaseline(snapshot.avg7Daily,
                snapshot.avg30Daily, snapshot.avg90Daily);
        snapshot.smoothingDailyBaseline = calculateSmoothingBaseline(snapshot.historySeries, 0.35, 120);
        snapshot.trendFactor = calculateTrendFactor(snapshot.avg7Daily, snapshot.avg30Daily, snapshot.avg90Daily);

        snapshot.monthFactors = computeMonthFactors(snapshot.historySeries);
        snapshot.seasonalityFactor = averageMonthFactor(snapshot.targetStart, snapshot.targetEnd, snapshot.monthFactors);

        populateMarketingSignals(snapshot);
        snapshot.marketingFactor = calculateMarketingFactor(snapshot.upcomingEvents.size(), snapshot.targetStart, snapshot.targetEnd);

        snapshot.weatherNarrative = buildWeatherNarrative(snapshot.targetStart, snapshot.targetEnd);
        snapshot.productSignals = loadProductSignals(em, snapshot, productId);
        snapshot.inventoryFactor = calculateInventoryFactor(snapshot.productSignals, productId);

        snapshot.forecastDailyRevenue = calculateForecastDailyRevenue(snapshot);
        snapshot.forecastRevenue = roundCurrency(snapshot.forecastDailyRevenue.multiply(BigDecimal.valueOf(snapshot.horizonDays)));
        snapshot.dailyStdDev = calculateStdDev(snapshot.historySeries, 90);
        snapshot.confidenceWidth = calculateConfidenceWidth(snapshot);
        snapshot.confidenceScore = calculateConfidenceScore(snapshot);
        snapshot.confidenceLow = floorAtZero(roundCurrency(snapshot.forecastRevenue.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(snapshot.confidenceWidth)))));
        snapshot.confidenceHigh = roundCurrency(snapshot.forecastRevenue.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(snapshot.confidenceWidth))));

        snapshot.seasonalityHighlights = buildSeasonalityHighlights(snapshot.monthFactors);
        populateSeasonalityDiagnostics(snapshot);

        snapshot.lowStockCount = (int) snapshot.productSignals.stream().filter(p -> p.needsReorder).count();
        snapshot.marginRiskCount = (int) snapshot.productSignals.stream().filter(p -> p.marginWarning).count();

        snapshot.notes.add("Backend đã chuẩn hóa chuỗi thời gian 365 ngày và tự động điền ngày thiếu bằng 0.");
        if (snapshot.daysWithSales < 60) {
            snapshot.notes.add("Dữ liệu bán hàng còn mỏng (< 60 ngày có giao dịch), khoảng tin cậy sẽ rộng hơn.");
        }
        if (!snapshot.upcomingEvents.isEmpty()) {
            snapshot.notes.add("Forecast đã cộng thêm tác động của sự kiện marketing trong cửa sổ dự báo.");
        }
        if (snapshot.lowStockCount > 0) {
            snapshot.notes.add("Forecast đã giảm nhẹ theo rủi ro hụt hàng ở các SKU bán nhanh.");
        }
        if (!snapshot.seasonalityDiagnostics.isEmpty()) {
            snapshot.notes.add("Seasonality được suy ra từ dữ liệu doanh thu lịch sử kết hợp knowledge base nông sản Việt Nam.");
        }

        return snapshot;
    }

    private String resolveScopeLabel(EntityManager em, Long productId) {
        if (productId == null) {
            return "Toàn bộ FreshMart";
        }
        return productRepo.findById(em, productId)
                .map(p -> "Sản phẩm: " + p.getName() + " (ID=" + productId + ")")
                .orElse("Sản phẩm ID=" + productId);
    }

    private Map<LocalDate, BigDecimal> loadRevenueSeries(EntityManager em,
                                                         LocalDate from,
                                                         LocalDate toInclusive,
                                                         Long productId) {
        Map<LocalDate, BigDecimal> series = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            series.put(d, BigDecimal.ZERO);
        }

        if (productId == null) {
            List<RevenueDaily> rows = revenueRepo.findBetween(em, from, toInclusive);
            for (RevenueDaily row : rows) {
                series.put(row.getRevenueDate(), safeAmount(row.getTotalRevenue()));
            }
            return series;
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                        "SELECT o.createdAt, oi.lineTotal FROM OrderItem oi JOIN oi.order o " +
                                "WHERE oi.product.id = :pid AND o.status = :status " +
                                "AND o.createdAt >= :fromTs AND o.createdAt < :toTs " +
                                "ORDER BY o.createdAt ASC")
                .setParameter("pid", productId)
                .setParameter("status", OrderStatus.COMPLETED)
                .setParameter("fromTs", from.atStartOfDay())
                .setParameter("toTs", toInclusive.plusDays(1).atStartOfDay())
                .getResultList();

        for (Object[] row : rows) {
            LocalDate day = ((LocalDateTime) row[0]).toLocalDate();
            BigDecimal lineTotal = safeAmount(toBigDecimal(row[1]));
            series.merge(day, lineTotal, BigDecimal::add);
        }
        return series;
    }

    private int countPositiveDays(Map<LocalDate, BigDecimal> series) {
        int count = 0;
        for (BigDecimal value : series.values()) {
            if (safeAmount(value).compareTo(BigDecimal.ZERO) > 0) {
                count++;
            }
        }
        return count;
    }

    private BigDecimal sumRange(Map<LocalDate, BigDecimal> series, LocalDate from, LocalDate toInclusive) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, BigDecimal> entry : series.entrySet()) {
            if (!entry.getKey().isBefore(from) && !entry.getKey().isAfter(toInclusive)) {
                sum = sum.add(safeAmount(entry.getValue()));
            }
        }
        return sum.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateWeightedDailyBaseline(BigDecimal avg7, BigDecimal avg30, BigDecimal avg90) {
        BigDecimal result = BigDecimal.ZERO;
        result = result.add(safeAmount(avg7).multiply(BigDecimal.valueOf(0.50)));
        result = result.add(safeAmount(avg30).multiply(BigDecimal.valueOf(0.30)));
        result = result.add(safeAmount(avg90).multiply(BigDecimal.valueOf(0.20)));
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSmoothingBaseline(Map<LocalDate, BigDecimal> series, double alpha, int maxDays) {
        List<LocalDate> dates = new ArrayList<>(series.keySet());
        dates.sort(LocalDate::compareTo);
        if (dates.size() > maxDays) {
            dates = dates.subList(dates.size() - maxDays, dates.size());
        }

        BigDecimal smoothing = null;
        BigDecimal a = BigDecimal.valueOf(alpha);
        BigDecimal oneMinusA = BigDecimal.ONE.subtract(a);

        for (LocalDate date : dates) {
            BigDecimal actual = safeAmount(series.get(date));
            if (smoothing == null) {
                smoothing = actual;
            } else {
                smoothing = a.multiply(actual).add(oneMinusA.multiply(smoothing));
            }
        }

        return smoothing == null ? BigDecimal.ZERO : smoothing.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTrendFactor(BigDecimal avg7, BigDecimal avg30, BigDecimal avg90) {
        double ratioShort = safeRatio(avg7, avg30, 1.0);
        double ratioMid = safeRatio(avg30, avg90, 1.0);
        double blended = (ratioShort * 0.60) + (ratioMid * 0.40);
        return BigDecimal.valueOf(clamp(blended, 0.85, 1.25)).setScale(4, RoundingMode.HALF_UP);
    }

    private Map<Integer, BigDecimal> computeMonthFactors(Map<LocalDate, BigDecimal> series) {
        Map<Integer, BigDecimal> monthSum = new HashMap<>();
        Map<Integer, Integer> monthCount = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (Map.Entry<LocalDate, BigDecimal> entry : series.entrySet()) {
            int month = entry.getKey().getMonthValue();
            BigDecimal value = safeAmount(entry.getValue());
            monthSum.merge(month, value, BigDecimal::add);
            monthCount.merge(month, 1, Integer::sum);
            total = total.add(value);
            count++;
        }

        BigDecimal globalAvg = divide(total, Math.max(1, count));
        Map<Integer, BigDecimal> factors = new LinkedHashMap<>();
        Map<Integer, BigDecimal> defaults = defaultMonthFactors();

        for (int month = 1; month <= 12; month++) {
            BigDecimal fallback = defaults.getOrDefault(month, BigDecimal.ONE);
            if (monthCount.containsKey(month) && globalAvg.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal monthAvg = divide(monthSum.get(month), monthCount.get(month));
                BigDecimal factor = monthAvg.divide(globalAvg, 4, RoundingMode.HALF_UP);
                factors.put(month, BigDecimal.valueOf(clamp(factor.doubleValue(), 0.75, 1.30)));
            } else {
                factors.put(month, fallback);
            }
        }
        return factors;
    }

    private Map<Integer, BigDecimal> defaultMonthFactors() {
        Map<Integer, BigDecimal> factors = new LinkedHashMap<>();
        factors.put(1, BigDecimal.valueOf(1.18));
        factors.put(2, BigDecimal.valueOf(1.12));
        factors.put(3, BigDecimal.valueOf(1.02));
        factors.put(4, BigDecimal.valueOf(1.00));
        factors.put(5, BigDecimal.valueOf(0.97));
        factors.put(6, BigDecimal.valueOf(0.95));
        factors.put(7, BigDecimal.valueOf(0.96));
        factors.put(8, BigDecimal.valueOf(0.99));
        factors.put(9, BigDecimal.valueOf(1.04));
        factors.put(10, BigDecimal.valueOf(1.07));
        factors.put(11, BigDecimal.valueOf(1.10));
        factors.put(12, BigDecimal.valueOf(1.16));
        return factors;
    }

    private BigDecimal averageMonthFactor(LocalDate targetStart,
                                          LocalDate targetEnd,
                                          Map<Integer, BigDecimal> monthFactors) {
        BigDecimal weightedSum = BigDecimal.ZERO;
        int weightedDays = 0;

        LocalDate cursor = targetStart;
        while (!cursor.isAfter(targetEnd)) {
            YearMonth ym = YearMonth.from(cursor);
            LocalDate monthEnd = ym.atEndOfMonth();
            LocalDate sliceEnd = monthEnd.isBefore(targetEnd) ? monthEnd : targetEnd;
            int days = (int) ChronoUnit.DAYS.between(cursor, sliceEnd) + 1;
            BigDecimal factor = monthFactors.getOrDefault(cursor.getMonthValue(), BigDecimal.ONE);
            weightedSum = weightedSum.add(factor.multiply(BigDecimal.valueOf(days)));
            weightedDays += days;
            cursor = sliceEnd.plusDays(1);
        }

        if (weightedDays <= 0) {
            return BigDecimal.ONE;
        }
        return weightedSum.divide(BigDecimal.valueOf(weightedDays), 4, RoundingMode.HALF_UP);
    }

    private void populateMarketingSignals(ForecastSnapshot snapshot) {
        LocalDate recentFrom = snapshot.generatedAt.minusDays(30);
        List<MarketingEvent> events = buildMarketingCalendar(snapshot.targetStart.getYear(), snapshot.targetEnd.getYear());

        for (MarketingEvent event : events) {
            if (!event.date.isBefore(snapshot.targetStart) && !event.date.isAfter(snapshot.targetEnd)) {
                long days = ChronoUnit.DAYS.between(snapshot.generatedAt, event.date);
                snapshot.upcomingEvents.add(String.format("%s - %s (còn %d ngày)",
                        event.name, event.date.format(DATE_FMT), days));
            }
            if (!event.date.isBefore(recentFrom) && !event.date.isAfter(snapshot.generatedAt)) {
                long daysAgo = ChronoUnit.DAYS.between(event.date, snapshot.generatedAt);
                snapshot.recentEvents.add(String.format("%s - %s (đã qua %d ngày)",
                        event.name, event.date.format(DATE_FMT), daysAgo));
            }
        }
    }

    private List<MarketingEvent> buildMarketingCalendar(int startYear, int endYear) {
        List<MarketingEvent> events = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            for (int month = 1; month <= 12; month++) {
                events.add(new MarketingEvent("Flash Sale " + month + "/" + month,
                        LocalDate.of(year, month, month), 0.03));
            }
            events.add(new MarketingEvent("Tết Nguyên Đán (mùa mua sắm)", LocalDate.of(year, 1, 25), 0.12));
            events.add(new MarketingEvent("Ngày Quốc tế Phụ nữ 8/3", LocalDate.of(year, 3, 8), 0.02));
            events.add(new MarketingEvent("Ngày Phụ nữ Việt Nam 20/10", LocalDate.of(year, 10, 20), 0.02));
            events.add(new MarketingEvent("Ngày Nhà giáo Việt Nam 20/11", LocalDate.of(year, 11, 20), 0.02));
            events.add(new MarketingEvent("Giáng sinh", LocalDate.of(year, 12, 24), 0.05));
        }
        return events;
    }

    private BigDecimal calculateMarketingFactor(int eventCount, LocalDate start, LocalDate end) {
        List<MarketingEvent> events = buildMarketingCalendar(start.getYear(), end.getYear());
        double lift = 0.0;
        for (MarketingEvent event : events) {
            if (!event.date.isBefore(start) && !event.date.isAfter(end)) {
                lift += event.lift;
            }
        }
        if (eventCount == 0) {
            lift += 0.0;
        }
        return BigDecimal.valueOf(1.0 + clamp(lift, 0.0, 0.18)).setScale(4, RoundingMode.HALF_UP);
    }

    private String buildWeatherNarrative(LocalDate targetStart, LocalDate targetEnd) {
        List<Integer> months = collectMonthsInRange(targetStart, targetEnd);
        if (months.stream().anyMatch(m -> m >= 9 && m <= 11)) {
            return "Khung dự báo rơi vào mùa mưa ở nhiều vùng: rau củ dễ khan hiếm hơn, giá nhập có xu hướng nhích lên và rủi ro chậm giao cao hơn.";
        }
        if (months.stream().anyMatch(m -> m >= 5 && m <= 8)) {
            return "Khung dự báo đi qua mùa hè: trái cây nhiệt đới dồi dào hơn, nhu cầu hàng giải nhiệt tăng, nhưng hao hụt bảo quản cũng cần theo dõi.";
        }
        if (months.stream().anyMatch(m -> m == 12 || m == 1 || m == 2)) {
            return "Khung dự báo chạm cuối năm/Tết: nhu cầu quà biếu, thực phẩm tươi và đơn gói combo thường tăng mạnh hơn nền bình quân.";
        }
        return "Khung dự báo nằm trong giai đoạn thời tiết tương đối ổn định, tác động thời tiết chủ yếu ở mức vừa phải.";
    }

    private List<Integer> collectMonthsInRange(LocalDate from, LocalDate toInclusive) {
        List<Integer> months = new ArrayList<>();
        LocalDate cursor = from.withDayOfMonth(1);
        while (!cursor.isAfter(toInclusive)) {
            months.add(cursor.getMonthValue());
            cursor = cursor.plusMonths(1);
        }
        return months;
    }

    private List<ProductSignal> loadProductSignals(EntityManager em, ForecastSnapshot snapshot, Long productId) {
        Map<Long, SalesAggregate> sales30 = loadSalesAggregate(em,
                snapshot.generatedAt.minusDays(29).atStartOfDay(),
                snapshot.generatedAt.plusDays(1).atStartOfDay(),
                productId);
        Map<Long, SalesAggregate> sales7 = loadSalesAggregate(em,
                snapshot.generatedAt.minusDays(6).atStartOfDay(),
                snapshot.generatedAt.plusDays(1).atStartOfDay(),
                productId);

        Map<Long, Product> productMap = productRepo.findAll(em, false).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Long> candidateIds = new ArrayList<>();
        if (productId != null) {
            candidateIds.add(productId);
        } else {
            candidateIds.addAll(sales30.entrySet().stream()
                    .sorted((a, b) -> b.getValue().revenue.compareTo(a.getValue().revenue))
                    .limit(MAX_PRODUCT_SIGNALS)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));
        }

        if (candidateIds.isEmpty()) {
            candidateIds.addAll(productMap.keySet().stream().limit(MAX_PRODUCT_SIGNALS).collect(Collectors.toList()));
        }

        List<ProductSignal> signals = new ArrayList<>();
        LocalDate today = snapshot.generatedAt;

        for (Long candidateId : candidateIds) {
            Product product = productMap.get(candidateId);
            if (product == null) {
                continue;
            }

            ProductSignal signal = new ProductSignal();
            signal.productId = product.getId();
            signal.productName = product.getName();
            signal.unit = safeText(product.getUnit(), "đơn vị");
            signal.sellPrice = safeAmount(product.getSellPrice());

            SalesAggregate aggregate30 = sales30.getOrDefault(candidateId, new SalesAggregate());
            SalesAggregate aggregate7 = sales7.getOrDefault(candidateId, new SalesAggregate());
            signal.soldQty30 = aggregate30.quantity;
            signal.soldQty7 = aggregate7.quantity;
            signal.revenue30 = aggregate30.revenue;
            signal.dailyDemand = divide(BigDecimal.valueOf(signal.soldQty30), 30);

            signal.stockQty = lotRepo.getAvailableQty(em, candidateId, today);
            signal.expiringQty = lotRepo.getExpiringQty(em, candidateId, today, 3);

            hydrateSupplierAndPriceSignals(em, signal, today);
            computeProcurementNeeds(signal);
            signals.add(signal);
        }

        signals.sort((a, b) -> Double.compare(priorityScore(b), priorityScore(a)));
        return signals;
    }

    private Map<Long, SalesAggregate> loadSalesAggregate(EntityManager em,
                                                         LocalDateTime from,
                                                         LocalDateTime to,
                                                         Long productId) {
        StringBuilder jpql = new StringBuilder(
                "SELECT oi.product.id, SUM(oi.quantity), SUM(oi.lineTotal) " +
                        "FROM OrderItem oi JOIN oi.order o " +
                        "WHERE o.status = :status AND o.createdAt >= :fromTs AND o.createdAt < :toTs ");
        if (productId != null) {
            jpql.append("AND oi.product.id = :pid ");
        }
        jpql.append("GROUP BY oi.product.id");

        var query = em.createQuery(jpql.toString(), Object[].class)
                .setParameter("status", OrderStatus.COMPLETED)
                .setParameter("fromTs", from)
                .setParameter("toTs", to);
        if (productId != null) {
            query.setParameter("pid", productId);
        }

        List<Object[]> rows = query.getResultList();
        Map<Long, SalesAggregate> out = new HashMap<>();
        for (Object[] row : rows) {
            Long pid = toLong(row[0]);
            SalesAggregate aggregate = new SalesAggregate();
            aggregate.quantity = toLong(row[1]);
            aggregate.revenue = safeAmount(toBigDecimal(row[2]));
            out.put(pid, aggregate);
        }
        return out;
    }

    private void hydrateSupplierAndPriceSignals(EntityManager em, ProductSignal signal, LocalDate today) {
        @SuppressWarnings("unchecked")
        List<Object[]> latestRows = em.createQuery(
                        "SELECT s.name, pl.importPrice, pl.importDate, s.leadTimeDays " +
                                "FROM ProductLot pl LEFT JOIN pl.supplier s " +
                                "WHERE pl.product.id = :pid " +
                                "ORDER BY pl.importDate DESC, pl.id DESC")
                .setParameter("pid", signal.productId)
                .setMaxResults(1)
                .getResultList();

        if (!latestRows.isEmpty()) {
            Object[] latest = latestRows.get(0);
            signal.supplierName = safeText((String) latest[0], "Nhà cung cấp gần nhất");
            signal.latestImportPrice = safeAmount(toBigDecimal(latest[1]));
            signal.latestImportDate = (LocalDate) latest[2];
            signal.leadTimeDays = latest[3] == null ? 1 : Math.max(1, ((Number) latest[3]).intValue());
        }

        @SuppressWarnings("unchecked")
        List<Object[]> oldestRows = em.createQuery(
                        "SELECT pl.importPrice, pl.importDate " +
                                "FROM ProductLot pl WHERE pl.product.id = :pid AND pl.importDate >= :fromDate " +
                                "ORDER BY pl.importDate ASC, pl.id ASC")
                .setParameter("pid", signal.productId)
                .setParameter("fromDate", today.minusDays(SUPPLIER_LOOKBACK_DAYS))
                .setMaxResults(1)
                .getResultList();

        if (!oldestRows.isEmpty()) {
            Object[] oldest = oldestRows.get(0);
            signal.oldestImportPrice = safeAmount(toBigDecimal(oldest[0]));
            signal.oldestImportDate = (LocalDate) oldest[1];
        }

        if (signal.oldestImportPrice.compareTo(BigDecimal.ZERO) > 0) {
            signal.importChangePct = signal.latestImportPrice.subtract(signal.oldestImportPrice)
                    .divide(signal.oldestImportPrice, 4, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED)
                    .setScale(1, RoundingMode.HALF_UP);
        }

        if (signal.sellPrice.compareTo(BigDecimal.ZERO) > 0 && signal.latestImportPrice.compareTo(BigDecimal.ZERO) > 0) {
            signal.marginPct = signal.sellPrice.subtract(signal.latestImportPrice)
                    .divide(signal.sellPrice, 4, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED)
                    .setScale(1, RoundingMode.HALF_UP);
        }

        signal.marginWarning = signal.marginPct.compareTo(BigDecimal.valueOf(18)) < 0
                || signal.importChangePct.compareTo(BigDecimal.valueOf(5)) > 0;
    }

    private void computeProcurementNeeds(ProductSignal signal) {
        BigDecimal demandDuringLeadTime = signal.dailyDemand.multiply(BigDecimal.valueOf(signal.leadTimeDays + PROCUREMENT_BUFFER_DAYS));
        BigDecimal safetyStock = signal.dailyDemand.multiply(BigDecimal.valueOf(SAFETY_STOCK_DAYS));
        BigDecimal targetStock = demandDuringLeadTime.add(safetyStock);
        BigDecimal currentStock = BigDecimal.valueOf(Math.max(0, signal.stockQty));
        BigDecimal rawReorder = targetStock.subtract(currentStock);
        signal.reorderQty = rawReorder.compareTo(BigDecimal.ZERO) > 0
                ? rawReorder.setScale(0, RoundingMode.CEILING)
                : BigDecimal.ZERO;

        if (signal.dailyDemand.compareTo(BigDecimal.ZERO) > 0) {
            signal.coverageDays = currentStock.divide(signal.dailyDemand, 1, RoundingMode.HALF_UP);
        } else {
            signal.coverageDays = BigDecimal.valueOf(signal.stockQty > 0 ? 999 : 0);
        }

        signal.needsReorder = signal.reorderQty.compareTo(BigDecimal.ZERO) > 0
                && (signal.stockQty == 0
                || signal.coverageDays.compareTo(BigDecimal.valueOf(signal.leadTimeDays + 1)) < 0);

        if (signal.stockQty == 0 && signal.soldQty30 > 0) {
            signal.reorderReason = "đã hết hàng nhưng vẫn có nhu cầu bán trong 30 ngày gần đây";
        } else if (signal.coverageDays.compareTo(BigDecimal.valueOf(signal.leadTimeDays)) < 0) {
            signal.reorderReason = "độ phủ kho thấp hơn lead time nhập hàng";
        } else if (signal.expiringQty > 0) {
            signal.reorderReason = "có một phần tồn kho sắp hết hạn nên cần nhập thận trọng";
        } else if (signal.importChangePct.compareTo(BigDecimal.valueOf(5)) > 0) {
            signal.reorderReason = "giá nhập đang tăng, nên chốt đơn sớm với NCC hiện tại";
        } else {
            signal.reorderReason = "mức tồn kho hiện tại chưa đủ cho lead time và safety stock";
        }
    }

    private double priorityScore(ProductSignal signal) {
        double score = signal.reorderQty.doubleValue();
        score += signal.soldQty30 * 0.20;
        score += signal.marginWarning ? 5.0 : 0.0;
        score += signal.stockQty == 0 ? 8.0 : 0.0;
        return score;
    }

    private BigDecimal calculateInventoryFactor(List<ProductSignal> signals, Long productId) {
        if (signals == null || signals.isEmpty()) {
            return BigDecimal.ONE;
        }

        double penalty = 0.0;
        if (productId != null) {
            ProductSignal signal = signals.get(0);
            if (signal.stockQty == 0 && signal.soldQty30 > 0) {
                penalty += 0.10;
            } else if (signal.needsReorder) {
                penalty += 0.05;
            }
            if (signal.expiringQty > 0) {
                penalty += 0.02;
            }
        } else {
            long highRisk = signals.stream().filter(s -> s.needsReorder || s.stockQty == 0).count();
            penalty += Math.min(0.10, highRisk * 0.02);
        }

        return BigDecimal.valueOf(1.0 - penalty).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateForecastDailyRevenue(ForecastSnapshot snapshot) {
        BigDecimal baseline = snapshot.weightedDailyBaseline.add(snapshot.smoothingDailyBaseline)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        baseline = baseline.multiply(snapshot.trendFactor)
                .multiply(snapshot.seasonalityFactor)
                .multiply(snapshot.marketingFactor)
                .multiply(snapshot.inventoryFactor);

        if (baseline.compareTo(BigDecimal.ZERO) == 0 && snapshot.avg30Daily.compareTo(BigDecimal.ZERO) > 0) {
            baseline = snapshot.avg30Daily
                    .multiply(snapshot.seasonalityFactor)
                    .multiply(snapshot.marketingFactor)
                    .multiply(snapshot.inventoryFactor);
        }
        return roundCurrency(baseline);
    }

    private double calculateStdDev(Map<LocalDate, BigDecimal> series, int lookbackDays) {
        if (series.isEmpty()) {
            return 0.0;
        }
        List<BigDecimal> values = new ArrayList<>(series.values());
        if (values.size() > lookbackDays) {
            values = values.subList(values.size() - lookbackDays, values.size());
        }

        double mean = values.stream()
                .mapToDouble(v -> safeAmount(v).doubleValue())
                .average()
                .orElse(0.0);
        if (mean == 0.0) {
            return 0.0;
        }

        double variance = 0.0;
        for (BigDecimal value : values) {
            double diff = safeAmount(value).doubleValue() - mean;
            variance += diff * diff;
        }
        variance = variance / Math.max(1, values.size());
        return Math.sqrt(variance);
    }

    private double calculateConfidenceWidth(ForecastSnapshot snapshot) {
        double mean = snapshot.avg90Daily.doubleValue() > 0 ? snapshot.avg90Daily.doubleValue() : 1.0;
        double cv = snapshot.dailyStdDev / mean;
        double penalty = 0.0;
        if (snapshot.daysWithSales < 60) {
            penalty += 0.06;
        }
        if (snapshot.productId != null) {
            penalty += 0.03;
        }
        if (snapshot.lowStockCount > 0) {
            penalty += 0.03;
        }
        return clamp(0.12 + (cv * 0.35) + penalty, 0.10, 0.38);
    }

    private double calculateConfidenceScore(ForecastSnapshot snapshot) {
        double mean = snapshot.avg90Daily.doubleValue() > 0 ? snapshot.avg90Daily.doubleValue() : 1.0;
        double cv = snapshot.dailyStdDev / mean;
        double penalty = 0.0;
        if (snapshot.daysWithSales < 60) {
            penalty += 0.08;
        }
        if (snapshot.productId != null) {
            penalty += 0.04;
        }
        if (snapshot.lowStockCount > 0) {
            penalty += 0.04;
        }
        double score = 0.88 - (cv * 0.30) - penalty;
        return clamp(score, 0.45, 0.92);
    }

    private List<String> buildSeasonalityHighlights(Map<Integer, BigDecimal> monthFactors) {
        List<Map.Entry<Integer, BigDecimal>> entries = new ArrayList<>(monthFactors.entrySet());
        List<String> highlights = new ArrayList<>();

        List<Map.Entry<Integer, BigDecimal>> peaks = entries.stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .collect(Collectors.toList());
        List<Map.Entry<Integer, BigDecimal>> dips = entries.stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(2)
                .collect(Collectors.toList());

        if (!peaks.isEmpty()) {
            highlights.add("Tháng mạnh nhất theo lịch sử: " + peaks.stream()
                    .map(e -> monthName(e.getKey()) + " (factor " + formatDecimal(e.getValue()) + "x)")
                    .collect(Collectors.joining(", ")));
        }
        if (!dips.isEmpty()) {
            highlights.add("Tháng yếu hơn nền: " + dips.stream()
                    .map(e -> monthName(e.getKey()) + " (factor " + formatDecimal(e.getValue()) + "x)")
                    .collect(Collectors.joining(", ")));
        }
        return highlights;
    }

    private void populateSeasonalityDiagnostics(ForecastSnapshot snapshot) {
        try {
            List<SeasonalityPoint> points = seasonalityService.analyze(365, 30, 1.5);
            List<SeasonalityMonthStat> monthStats = seasonalityService.summarizeByMonth(points);
            long peakCount = points.stream().filter(p -> "PEAK".equals(p.getSignal())).count();
            long dipCount = points.stream().filter(p -> "DIP".equals(p.getSignal())).count();

            snapshot.seasonalityDiagnostics.add("Tín hiệu bất thường 365 ngày: " + peakCount + " ngày PEAK, " + dipCount + " ngày DIP.");
            for (SeasonalityMonthStat stat : monthStats.stream().limit(4).collect(Collectors.toList())) {
                snapshot.seasonalityDiagnostics.add("Tháng " + stat.getMonth() + ": TB="
                        + formatVND(stat.getAvgDemand()) + ", Min=" + formatVND(stat.getMinDemand())
                        + ", Max=" + formatVND(stat.getMaxDemand()));
            }
        } catch (Exception e) {
            snapshot.seasonalityDiagnostics.add("Chưa đủ dữ liệu để chạy seasonality diagnostics nâng cao.");
        }
    }

    private String renderSnapshot(ForecastSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DỮ LIỆU ĐẦU VÀO CHO AI ENGINE DỰ BÁO DOANH THU ===\n");
        sb.append("Ngày phân tích: ").append(snapshot.generatedAt.format(DATE_FMT)).append("\n");
        sb.append("Phạm vi: ").append(snapshot.scopeLabel).append("\n");
        sb.append("Kỳ dự báo: ").append(translatePeriod(snapshot.period)).append(" (")
                .append(snapshot.targetStart.format(DATE_FMT)).append(" - ")
                .append(snapshot.targetEnd.format(DATE_FMT)).append(")\n\n");

        sb.append("--- 1. TIỀN XỬ LÝ (Preprocessing) ---\n");
        sb.append("Chuỗi thời gian đã chuẩn hóa: ").append(snapshot.historyDays).append(" ngày liên tục\n");
        sb.append("Số ngày có phát sinh doanh thu: ").append(snapshot.daysWithSales).append(" ngày\n");
        sb.append("Doanh thu 7 ngày gần nhất: ").append(formatVND(snapshot.last7Revenue)).append("\n");
        sb.append("Doanh thu 30 ngày gần nhất: ").append(formatVND(snapshot.last30Revenue)).append("\n");
        sb.append("Doanh thu 90 ngày gần nhất: ").append(formatVND(snapshot.last90Revenue)).append("\n");
        sb.append("TB/ngày 7d | 30d | 90d: ")
                .append(formatVND(snapshot.avg7Daily)).append(" | ")
                .append(formatVND(snapshot.avg30Daily)).append(" | ")
                .append(formatVND(snapshot.avg90Daily)).append("\n");
        sb.append("Baseline backend (weighted | ES): ")
                .append(formatVND(snapshot.weightedDailyBaseline)).append(" | ")
                .append(formatVND(snapshot.smoothingDailyBaseline)).append("\n\n");

        sb.append("--- 2. TÍN HIỆU MÔ HÌNH (Model Inputs) ---\n");
        sb.append("Hệ số trend: ").append(formatDecimal(snapshot.trendFactor)).append("x\n");
        sb.append("Hệ số mùa vụ: ").append(formatDecimal(snapshot.seasonalityFactor)).append("x\n");
        sb.append("Hệ số marketing: ").append(formatDecimal(snapshot.marketingFactor)).append("x\n");
        sb.append("Hệ số tồn kho/supply: ").append(formatDecimal(snapshot.inventoryFactor)).append("x\n");
        sb.append("Dự báo backend cho kỳ tới: ").append(formatVND(snapshot.forecastRevenue)).append("\n");
        sb.append("Khoảng tin cậy backend: ").append(formatVND(snapshot.confidenceLow))
                .append(" - ").append(formatVND(snapshot.confidenceHigh)).append("\n\n");

        sb.append("--- 3. BIẾN ĐỘNG GIÁ NHÀ CUNG CẤP & KHO ---\n");
        if (snapshot.productSignals.isEmpty()) {
            sb.append("Chưa có signal sản phẩm đủ mạnh để phân tích nhập hàng.\n");
        } else {
            for (ProductSignal signal : snapshot.productSignals) {
                sb.append("• ").append(signal.productName)
                        .append(": bán 30d=").append(signal.soldQty30).append(" ").append(signal.unit)
                        .append(", tồn=").append(signal.stockQty).append(" ").append(signal.unit)
                        .append(", NCC=").append(signal.supplierName)
                        .append(", giá nhập=").append(formatVND(signal.latestImportPrice))
                        .append(", margin=").append(formatPercent(signal.marginPct))
                        .append(", biến động giá=").append(formatSignedPercent(signal.importChangePct))
                        .append(", reorder=").append(signal.reorderQty.toPlainString()).append(" ").append(signal.unit)
                        .append("\n");
            }
        }
        sb.append("\n");

        sb.append("--- 4. SỰ KIỆN MARKETING ---\n");
        if (!snapshot.upcomingEvents.isEmpty()) {
            sb.append("Sự kiện nằm trong cửa sổ dự báo:\n");
            snapshot.upcomingEvents.forEach(event -> sb.append("  - ").append(event).append("\n"));
        } else {
            sb.append("Không có flash-sale/ngày lễ lớn nằm trọn trong cửa sổ dự báo.\n");
        }
        if (!snapshot.recentEvents.isEmpty()) {
            sb.append("Sự kiện gần đây (để tham chiếu hiệu ứng dư âm):\n");
            snapshot.recentEvents.forEach(event -> sb.append("  - ").append(event).append("\n"));
        }
        sb.append("\n");

        sb.append("--- 5. MÙA VỤ & THỜI TIẾT ---\n");
        snapshot.seasonalityHighlights.forEach(line -> sb.append("- ").append(line).append("\n"));
        snapshot.seasonalityDiagnostics.forEach(line -> sb.append("- ").append(line).append("\n"));
        sb.append("- Tín hiệu thời tiết theo mùa: ").append(snapshot.weatherNarrative).append("\n");
        sb.append("- Hôm nay là ").append(translateDayOfWeek(snapshot.generatedAt.getDayOfWeek()));
        if (snapshot.generatedAt.getDayOfWeek() == DayOfWeek.SATURDAY
                || snapshot.generatedAt.getDayOfWeek() == DayOfWeek.SUNDAY) {
            sb.append(" (cuối tuần - doanh thu thường tăng 10-15%)");
        }
        sb.append("\n\n");

        sb.append("--- 6. GHI CHÚ ENGINE ---\n");
        snapshot.notes.forEach(note -> sb.append("- ").append(note).append("\n"));
        return sb.toString();
    }

    private String normalizePeriod(String period) {
        if (period == null) {
            return "month";
        }
        String safe = period.trim().toLowerCase(Locale.ROOT);
        if ("quarter".equals(safe) || "year".equals(safe)) {
            return safe;
        }
        return "month";
    }

    private LocalDate resolveTargetStart(LocalDate today, String period) {
        switch (normalizePeriod(period)) {
            case "quarter": {
                int currentQuarter = ((today.getMonthValue() - 1) / 3) + 1;
                int nextQuarter = currentQuarter + 1;
                int year = today.getYear();
                if (nextQuarter > 4) {
                    nextQuarter = 1;
                    year++;
                }
                int startMonth = ((nextQuarter - 1) * 3) + 1;
                return LocalDate.of(year, startMonth, 1);
            }
            case "year":
                return LocalDate.of(today.getYear() + 1, 1, 1);
            default:
                return today.with(TemporalAdjusters.firstDayOfNextMonth());
        }
    }

    private LocalDate resolveTargetEnd(LocalDate targetStart, String period) {
        switch (normalizePeriod(period)) {
            case "quarter":
                return targetStart.plusMonths(3).minusDays(1);
            case "year":
                return targetStart.plusYears(1).minusDays(1);
            default:
                return targetStart.plusMonths(1).minusDays(1);
        }
    }

    private LocalDate resolvePreviousPeriodStart(LocalDate targetStart, String period) {
        switch (normalizePeriod(period)) {
            case "quarter":
                return targetStart.minusMonths(3);
            case "year":
                return targetStart.minusYears(1);
            default:
                return targetStart.minusMonths(1);
        }
    }

    private BigDecimal divide(BigDecimal total, int divisor) {
        if (divisor <= 0) {
            return BigDecimal.ZERO;
        }
        return safeAmount(total).divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal roundCurrency(BigDecimal amount) {
        return safeAmount(amount).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal floorAtZero(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : amount;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String safeText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private double safeRatio(BigDecimal numerator, BigDecimal denominator, double fallback) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return fallback;
        }
        return safeAmount(numerator).divide(denominator, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String formatVND(BigDecimal amount) {
        return VND_FORMAT.format(safeAmount(amount)) + " VND";
    }

    private String formatPercent(BigDecimal amount) {
        return safeAmount(amount).setScale(1, RoundingMode.HALF_UP) + "%";
    }

    private String formatSignedPercent(BigDecimal amount) {
        BigDecimal safe = safeAmount(amount).setScale(1, RoundingMode.HALF_UP);
        String prefix = safe.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return prefix + safe + "%";
    }

    private String formatDecimal(BigDecimal amount) {
        return safeAmount(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String translatePeriod(String period) {
        switch (normalizePeriod(period)) {
            case "quarter":
                return "Quý tới";
            case "year":
                return "Năm tới";
            default:
                return "Tháng tới";
        }
    }

    private String translateDayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "Thứ Hai";
            case TUESDAY:
                return "Thứ Ba";
            case WEDNESDAY:
                return "Thứ Tư";
            case THURSDAY:
                return "Thứ Năm";
            case FRIDAY:
                return "Thứ Sáu";
            case SATURDAY:
                return "Thứ Bảy";
            case SUNDAY:
                return "Chủ Nhật";
            default:
                return dayOfWeek.name();
        }
    }

    private String monthName(int month) {
        return "Tháng " + month;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static final class SalesAggregate {
        long quantity;
        BigDecimal revenue = BigDecimal.ZERO;
    }

    private static final class MarketingEvent {
        private final String name;
        private final LocalDate date;
        private final double lift;

        private MarketingEvent(String name, LocalDate date, double lift) {
            this.name = name;
            this.date = date;
            this.lift = lift;
        }
    }

    public static final class ProductSignal {
        public Long productId;
        public String productName = "";
        public String unit = "đơn vị";
        public String supplierName = "Nhà cung cấp gần nhất";
        public BigDecimal sellPrice = BigDecimal.ZERO;
        public BigDecimal latestImportPrice = BigDecimal.ZERO;
        public LocalDate latestImportDate;
        public BigDecimal oldestImportPrice = BigDecimal.ZERO;
        public LocalDate oldestImportDate;
        public BigDecimal importChangePct = BigDecimal.ZERO;
        public BigDecimal marginPct = BigDecimal.ZERO;
        public BigDecimal revenue30 = BigDecimal.ZERO;
        public long soldQty7;
        public long soldQty30;
        public int stockQty;
        public int expiringQty;
        public int leadTimeDays = 1;
        public BigDecimal dailyDemand = BigDecimal.ZERO;
        public BigDecimal reorderQty = BigDecimal.ZERO;
        public BigDecimal coverageDays = BigDecimal.ZERO;
        public boolean needsReorder;
        public boolean marginWarning;
        public String reorderReason = "";
    }

    public static final class ForecastSnapshot {
        public String period = "month";
        public Long productId;
        public String scopeLabel = "Toàn bộ FreshMart";
        public LocalDate generatedAt = LocalDate.now();
        public LocalDate historyStart = LocalDate.now().minusDays(HISTORY_DAYS - 1L);
        public LocalDate targetStart;
        public LocalDate targetEnd;
        public LocalDate previousPeriodStart;
        public LocalDate previousPeriodEnd;
        public int historyDays;
        public int daysWithSales;
        public int horizonDays;
        public int lowStockCount;
        public int marginRiskCount;
        public BigDecimal last7Revenue = BigDecimal.ZERO;
        public BigDecimal last30Revenue = BigDecimal.ZERO;
        public BigDecimal last90Revenue = BigDecimal.ZERO;
        public BigDecimal avg7Daily = BigDecimal.ZERO;
        public BigDecimal avg30Daily = BigDecimal.ZERO;
        public BigDecimal avg90Daily = BigDecimal.ZERO;
        public BigDecimal previousComparableRevenue = BigDecimal.ZERO;
        public BigDecimal weightedDailyBaseline = BigDecimal.ZERO;
        public BigDecimal smoothingDailyBaseline = BigDecimal.ZERO;
        public BigDecimal trendFactor = BigDecimal.ONE;
        public BigDecimal seasonalityFactor = BigDecimal.ONE;
        public BigDecimal marketingFactor = BigDecimal.ONE;
        public BigDecimal inventoryFactor = BigDecimal.ONE;
        public BigDecimal forecastDailyRevenue = BigDecimal.ZERO;
        public BigDecimal forecastRevenue = BigDecimal.ZERO;
        public BigDecimal confidenceLow = BigDecimal.ZERO;
        public BigDecimal confidenceHigh = BigDecimal.ZERO;
        public double dailyStdDev;
        public double confidenceWidth;
        public double confidenceScore;
        public String weatherNarrative = "";
        public Map<LocalDate, BigDecimal> historySeries = new LinkedHashMap<>();
        public Map<Integer, BigDecimal> monthFactors = new LinkedHashMap<>();
        public List<ProductSignal> productSignals = new ArrayList<>();
        public List<String> upcomingEvents = new ArrayList<>();
        public List<String> recentEvents = new ArrayList<>();
        public List<String> seasonalityHighlights = new ArrayList<>();
        public List<String> seasonalityDiagnostics = new ArrayList<>();
        public List<String> notes = new ArrayList<>();
    }
}

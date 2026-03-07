package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.enums.OrderStatus;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.service.dto.ReplenishSuggestion;
import com.freshmart.util.JpaExecutor;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReplenishmentService {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();

    /**
     * Rule-based replenishment suggestion.
     *
     * daysHistory: lấy lịch sử bán trong N ngày (vd 30)
     * leadTimeDays: thời gian nhập hàng về (vd 3)
     * bufferDays: buffer thêm cho delay (vd 1-2)
     * safetyDays: tồn kho an toàn (vd 2)
     */
    public List<ReplenishSuggestion> suggest(int daysHistory, int leadTimeDays, int bufferDays, int safetyDays) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            List<Product> products = productRepo.findAll(em, false);

            BigDecimal seasonFactor = getSeasonFactor(today);

            // Rule: expiring window (<= 3 days)
            final int EXPIRING_DAYS = 3;

            List<ReplenishSuggestion> out = new ArrayList<>();
            for (Product p : products) {
                BigDecimal avg7 = avgDailySold(em, p.getId(), 7, today);
                BigDecimal avgHistory = avgDailySold(em, p.getId(), daysHistory, today);

                // trend = avg7 / avgHistory (nếu avgHistory=0 thì trend=1)
                BigDecimal trend = BigDecimal.ONE;
                if (avgHistory.compareTo(BigDecimal.ZERO) > 0) {
                    trend = avg7.divide(avgHistory, 4, RoundingMode.HALF_UP);

                    // clamp trend to [0.5 .. 2.0]
                    if (trend.compareTo(new BigDecimal("0.5")) < 0) trend = new BigDecimal("0.5");
                    if (trend.compareTo(new BigDecimal("2.0")) > 0) trend = new BigDecimal("2.0");
                }

                // forecastPerDay = avg7 * trend * seasonFactor
                BigDecimal forecastPerDay = avg7.multiply(trend).multiply(seasonFactor)
                        .setScale(2, RoundingMode.HALF_UP);

                int stock = lotRepo.getAvailableQty(em, p.getId(), today);

                // expectedDemand = forecastPerDay * (leadTimeDays + bufferDays)
                // safetyStock    = forecastPerDay * safetyDays
                // reorderPoint   = expectedDemand + safetyStock
                BigDecimal expectedDemand = forecastPerDay.multiply(BigDecimal.valueOf(leadTimeDays + bufferDays));
                BigDecimal safetyStock = forecastPerDay.multiply(BigDecimal.valueOf(safetyDays));
                BigDecimal reorderPoint = expectedDemand.add(safetyStock);

                int suggestedQty = reorderPoint.setScale(0, RoundingMode.CEILING).intValue() - stock;
                if (suggestedQty < 0) suggestedQty = 0;

                // NEW: expiring lots logic (<= 3 days)
                int expiringQty = lotRepo.getExpiringQty(em, p.getId(), today, EXPIRING_DAYS);
                int expiringLots = lotRepo.countExpiringLots(em, p.getId(), today, EXPIRING_DAYS);

                // Rule-based adjustment:
                // If there are "many" expiring lots OR expiring qty is a big portion of stock => reduce suggested qty
                String note = null;
                if (stock > 0) {
                    BigDecimal expiringRatio = BigDecimal.valueOf(expiringQty)
                            .divide(BigDecimal.valueOf(stock), 4, RoundingMode.HALF_UP);

                    boolean manyExpiringLots = expiringLots >= 2;
                    boolean bigExpiringQty = expiringRatio.compareTo(new BigDecimal("0.30")) >= 0; // >= 30%

                    if ((manyExpiringLots || bigExpiringQty) && suggestedQty > 0) {
                        int old = suggestedQty;

                        // Reduce by 50% (rule-based demo)
                        suggestedQty = (int) Math.ceil(old * 0.5);

                        note = "Lots expiring <= " + EXPIRING_DAYS + "d: " + expiringLots +
                                " lots / " + expiringQty + " units. Reduce suggestion to prioritize clearance.";
                    } else if (expiringQty > 0 || expiringLots > 0) {
                        note = "Lots expiring <= " + EXPIRING_DAYS + "d: " + expiringLots +
                                " lots / " + expiringQty + " units.";
                    }
                } else {
                    // stock == 0, still show expiring info if any (usually 0)
                    if (expiringQty > 0 || expiringLots > 0) {
                        note = "Lots expiring <= " + EXPIRING_DAYS + "d: " + expiringLots +
                                " lots / " + expiringQty + " units.";
                    }
                }

                out.add(new ReplenishSuggestion(
                        p.getId(),
                        p.getName(),
                        avg7, avgHistory,     // avgHistory đang dùng làm avg30 (hoặc daysHistory)
                        seasonFactor,
                        forecastPerDay,
                        stock,
                        suggestedQty,
                        expiringQty,
                        expiringLots,
                        note
                ));
            }

            out.sort((a, b) -> Integer.compare(b.getSuggestedQty(), a.getSuggestedQty()));
            return out;
        });
    }

    /**
     * avgDailySold = tổng quantity bán / số ngày.
     * Lấy từ order_items join orders (COMPLETED) theo created_at.
     */
    private BigDecimal avgDailySold(EntityManager em, Long productId, int days, LocalDate today) {
        LocalDateTime to = today.plusDays(1).atStartOfDay();        // exclusive
        LocalDateTime from = today.minusDays(days).atStartOfDay();  // inclusive

        Long sumQty = em.createQuery(
                        "SELECT COALESCE(SUM(oi.quantity), 0) " +
                                "FROM OrderItem oi JOIN oi.order o " +
                                "WHERE oi.product.id = :pid " +
                                "AND o.status = :st " +
                                "AND o.createdAt >= :from AND o.createdAt < :to",
                        Long.class
                )
                .setParameter("pid", productId)
                .setParameter("st", OrderStatus.COMPLETED)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return BigDecimal.valueOf(sumQty)
                .divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
    }

    /**
     * Season rules (bạn có thể chỉnh theo báo cáo):
     * - Tháng 12,1 (Tết/đầu năm): +30%
     * - Tháng 5-8 (mùa nóng): +15%
     * - Cuối tuần (T7,CN): +10%
     */
    private BigDecimal getSeasonFactor(LocalDate today) {
        BigDecimal factor = BigDecimal.ONE;
        int m = today.getMonthValue();

        if (m == 12 || m == 1) factor = factor.multiply(new BigDecimal("1.30"));
        if (m >= 5 && m <= 8) factor = factor.multiply(new BigDecimal("1.15"));

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            factor = factor.multiply(new BigDecimal("1.10"));
        }

        return factor.setScale(2, RoundingMode.HALF_UP);
    }
}
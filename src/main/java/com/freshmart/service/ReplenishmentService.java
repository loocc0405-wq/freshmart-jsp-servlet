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
     * safetyDays: tồn kho an toàn (vd 2)
     */
    public List<ReplenishSuggestion> suggest(int daysHistory, int leadTimeDays, int safetyDays) {
        return executor.execute(em -> {
            LocalDate today = LocalDate.now();
            List<Product> products = productRepo.findAll(em);

            BigDecimal seasonFactor = getSeasonFactor(today);

            List<ReplenishSuggestion> out = new ArrayList<>();
            for (Product p : products) {
                BigDecimal avg7 = avgDailySold(em, p.getId(), 7);
                BigDecimal avg30 = avgDailySold(em, p.getId(), daysHistory);

                // trend = avg7 / avg30 (nếu avg30=0 thì trend=1)
                BigDecimal trend = BigDecimal.ONE;
                if (avg30.compareTo(BigDecimal.ZERO) > 0) {
                    trend = avg7.divide(avg30, 4, RoundingMode.HALF_UP);
                    // kẹp trend để tránh nhảy quá mạnh
                    if (trend.compareTo(new BigDecimal("0.5")) < 0) trend = new BigDecimal("0.5");
                    if (trend.compareTo(new BigDecimal("2.0")) > 0) trend = new BigDecimal("2.0");
                }

                // forecastPerDay = avg7 * trend * seasonFactor
                BigDecimal forecastPerDay = avg7.multiply(trend).multiply(seasonFactor)
                        .setScale(2, RoundingMode.HALF_UP);

                int stock = lotRepo.getAvailableQty(em, p.getId(), today);

                // Need = forecastPerDay * (leadTimeDays + safetyDays) - stock
                BigDecimal need = forecastPerDay.multiply(BigDecimal.valueOf(leadTimeDays + safetyDays));
                int suggestedQty = need.setScale(0, RoundingMode.CEILING).intValue() - stock;
                if (suggestedQty < 0) suggestedQty = 0;

                out.add(new ReplenishSuggestion(
                        p.getId(),
                        p.getName(),
                        avg7, avg30,
                        seasonFactor,
                        forecastPerDay,
                        stock,
                        suggestedQty
                ));
            }

            // sort: suggested desc
            out.sort((a, b) -> Integer.compare(b.getSuggestedQty(), a.getSuggestedQty()));
            return out;
        });
    }

    /**
     * avgDailySold = tổng quantity bán / số ngày.
     * Lấy từ order_items join orders (COMPLETED) theo created_at.
     */
    private BigDecimal avgDailySold(EntityManager em, Long productId, int days) {
        LocalDateTime to = LocalDate.now().plusDays(1).atStartOfDay();      // exclusive
        LocalDateTime from = LocalDate.now().minusDays(days).atStartOfDay(); // inclusive

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
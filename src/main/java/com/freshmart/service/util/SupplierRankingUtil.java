package com.freshmart.service.util;

import com.freshmart.service.dto.SupplierCandidate;

import java.util.Comparator;
import java.util.List;

/**
 * Shared ranking logic cho supplier candidates.
 * Dùng chung bởi ReplenishmentService và ProductHealthService.
 *
 * Rule ưu tiên:
 * 1. leadTimeDays nhỏ hơn
 * 2. avgImportPrice thấp hơn
 * 3. lastImportDate mới hơn
 * 4. lotCount nhiều hơn
 * 5. totalQtyIn nhiều hơn
 * 6. supplierId nhỏ hơn (tiebreaker)
 */
public final class SupplierRankingUtil {

    private SupplierRankingUtil() {}

    /**
     * Trả về supplier tốt nhất từ danh sách candidates.
     * Trả về null nếu list rỗng hoặc null.
     * Lưu ý: method này sort in-place trên list truyền vào.
     */
    public static SupplierCandidate pickBest(List<SupplierCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) return null;

        candidates.sort(Comparator
                .comparing(SupplierCandidate::getSupplierLeadTimeDays,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SupplierCandidate::getAvgImportPrice,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SupplierCandidate::getLastImportDate,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(SupplierCandidate::getLotCount, Comparator.reverseOrder())
                .thenComparing(SupplierCandidate::getTotalQtyIn, Comparator.reverseOrder())
                .thenComparing(SupplierCandidate::getSupplierId));

        return candidates.get(0);
    }
}

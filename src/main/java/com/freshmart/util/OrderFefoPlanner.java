package com.freshmart.util;

import com.freshmart.entity.ProductLot;
import com.freshmart.service.dto.FefoAllocationLot;
import com.freshmart.service.dto.FefoAllocationPlan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class OrderFefoPlanner {

    private OrderFefoPlanner() {
    }

    public static FefoAllocationPlan buildPlan(List<ProductLot> rawLots,
                                               int requestedQty,
                                               LocalDate today,
                                               int nearExpiryDays) {
        List<ProductLot> lots = FEFOUtil.getAvailableLotsFEFO(rawLots, today);
        List<FefoAllocationLot> allocations = new ArrayList<>();

        int safeRequestedQty = Math.max(0, requestedQty);
        int availableQty = lots.stream().mapToInt(ProductLot::getQtyLeft).sum();
        int nearExpiryQty = lots.stream()
                .filter(lot -> FEFOUtil.needsUrgentUse(lot, today, nearExpiryDays))
                .mapToInt(ProductLot::getQtyLeft)
                .sum();
        LocalDate nearestExpiry = lots.stream()
                .map(ProductLot::getExpiryDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        int need = safeRequestedQty;
        boolean usesNearExpiryLots = false;

        for (ProductLot lot : lots) {
            if (need <= 0) break;

            int take = Math.min(need, lot.getQtyLeft());
            if (take <= 0) continue;

            long daysUntilExpiry = FEFOUtil.getDaysUntilExpiry(lot, today);
            boolean nearExpiry = FEFOUtil.needsUrgentUse(lot, today, nearExpiryDays);
            if (nearExpiry) {
                usesNearExpiryLots = true;
            }

            allocations.add(new FefoAllocationLot(
                    lot.getId(),
                    lot.getImportDate(),
                    lot.getExpiryDate(),
                    lot.getQtyLeft(),
                    take,
                    daysUntilExpiry,
                    nearExpiry
            ));
            need -= take;
        }

        int shortageQty = Math.max(0, need);
        return new FefoAllocationPlan(
                safeRequestedQty,
                availableQty,
                nearExpiryQty,
                shortageQty,
                nearestExpiry,
                shortageQty == 0,
                usesNearExpiryLots,
                allocations
        );
    }
}

package com.freshmart.service.dto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class FefoAllocationPlan {
    private final int requestedQty;
    private final int availableQty;
    private final int nearExpiryQty;
    private final int shortageQty;
    private final LocalDate nearestExpiry;
    private final boolean enoughStock;
    private final boolean usesNearExpiryLots;
    private final List<FefoAllocationLot> allocations;

    public FefoAllocationPlan(int requestedQty,
                              int availableQty,
                              int nearExpiryQty,
                              int shortageQty,
                              LocalDate nearestExpiry,
                              boolean enoughStock,
                              boolean usesNearExpiryLots,
                              List<FefoAllocationLot> allocations) {
        this.requestedQty = requestedQty;
        this.availableQty = availableQty;
        this.nearExpiryQty = nearExpiryQty;
        this.shortageQty = shortageQty;
        this.nearestExpiry = nearestExpiry;
        this.enoughStock = enoughStock;
        this.usesNearExpiryLots = usesNearExpiryLots;
        this.allocations = allocations == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(allocations);
    }

    public int getRequestedQty() { return requestedQty; }
    public int getAvailableQty() { return availableQty; }
    public int getNearExpiryQty() { return nearExpiryQty; }
    public int getShortageQty() { return shortageQty; }
    public LocalDate getNearestExpiry() { return nearestExpiry; }
    public boolean isEnoughStock() { return enoughStock; }
    public boolean isUsesNearExpiryLots() { return usesNearExpiryLots; }
    public List<FefoAllocationLot> getAllocations() { return allocations; }
}

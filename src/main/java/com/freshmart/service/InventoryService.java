package com.freshmart.service;

import com.freshmart.entity.ProductLot;
import com.freshmart.exception.InsufficientStockException;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.LotConsumption;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {

    private final ProductLotRepository lotRepo = new ProductLotRepository();

    public int getAvailableQty(EntityManager em, Long productId, LocalDate today) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (today == null) {
            throw new IllegalArgumentException("Today is required");
        }
        return lotRepo.getAvailableQty(em, productId, today);
    }

    /**
     * FEFO (First Expired, First Out) stock deduction.
     * This method MUST be executed inside a transaction.
     *
     * Improvement over the previous version:
     * - lock lots first,
     * - calculate available quantity on the locked rows,
     * - then deduct in FEFO order.
     *
     * This avoids the classic double-read race where one transaction checks stock
     * before another transaction has finished consuming it.
     */
    public List<LotConsumption> consumeStockFEFO(EntityManager em, Long productId, int qty, LocalDate today) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (today == null) {
            throw new IllegalArgumentException("Today is required");
        }
        if (qty <= 0) {
            return List.of();
        }

        List<ProductLot> lockedLots = lotRepo.findAvailableLotsFEFOForUpdate(em, productId, today);
        int lockedAvailable = lockedLots.stream()
                .mapToInt(lot -> lot.getQtyLeft() == null ? 0 : lot.getQtyLeft())
                .sum();

        if (lockedAvailable < qty) {
            throw new InsufficientStockException(
                    "Not enough stock. Need=" + qty + ", available=" + lockedAvailable
            );
        }

        List<LotConsumption> consumed = new ArrayList<>();
        int remaining = qty;

        for (ProductLot lot : lockedLots) {
            if (remaining <= 0) {
                break;
            }

            int qtyLeft = lot.getQtyLeft() == null ? 0 : lot.getQtyLeft();
            if (qtyLeft <= 0) {
                continue;
            }

            int take = Math.min(remaining, qtyLeft);
            lot.setQtyLeft(qtyLeft - take);
            em.merge(lot);

            consumed.add(new LotConsumption(lot.getId(), take, lot.getExpiryDate()));
            remaining -= take;
        }

        if (remaining > 0) {
            throw new InsufficientStockException(
                    "Stock deduction failed after locking lots. Remaining=" + remaining
            );
        }

        return consumed;
    }
}
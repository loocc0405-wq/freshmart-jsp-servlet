package com.freshmart.service;

import com.freshmart.entity.ProductLot;
import com.freshmart.exception.InsufficientStockException;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.LotConsumption;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {

    private final ProductLotRepository lotRepo = new ProductLotRepository();

    public int getAvailableQty(EntityManager em, Long productId, LocalDate today) {
        return lotRepo.getAvailableQty(em, productId, today);
    }

    /**
     * FEFO (First Expired, First Out) stock deduction.
     * This method MUST be executed inside a transaction.
     */
    public List<LotConsumption> consumeStockFEFO(EntityManager em, Long productId, int qty, LocalDate today) {
        if (qty <= 0) return List.of();

        int available = lotRepo.getAvailableQty(em, productId, today);
        if (available < qty) {
            throw new InsufficientStockException("Not enough stock. Need=" + qty + ", available=" + available);
        }

        List<ProductLot> lots = lotRepo.findAvailableLotsFEFO(em, productId, today);
        List<LotConsumption> consumed = new ArrayList<>();

        int remaining = qty;
        for (ProductLot lot : lots) {
            if (remaining == 0) break;

            int take = Math.min(remaining, lot.getQtyLeft());
            lot.setQtyLeft(lot.getQtyLeft() - take);
            em.merge(lot);

            consumed.add(new LotConsumption(lot.getId(), take, lot.getExpiryDate()));
            remaining -= take;
        }

        if (remaining != 0) {
            // Should not happen since we checked available qty before,
            // but keep it as safety guard.
            throw new InsufficientStockException("Stock deduction failed. Remaining=" + remaining);
        }

        return consumed;
    }
}

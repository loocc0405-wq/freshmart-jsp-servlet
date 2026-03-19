package com.freshmart.service;

import com.freshmart.entity.InventoryTransaction;
import com.freshmart.entity.LotDisposal;
import com.freshmart.entity.OrderItem;
import com.freshmart.entity.OrderItemLotAllocation;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.User;
import com.freshmart.enums.InventoryTransactionType;
import com.freshmart.repository.InventoryTransactionRepository;
import com.freshmart.repository.LotDisposalRepository;
import com.freshmart.repository.OrderItemLotAllocationRepository;
import com.freshmart.service.dto.LotConsumption;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryAuditService {

    private final OrderItemLotAllocationRepository allocationRepository = new OrderItemLotAllocationRepository();
    private final InventoryTransactionRepository transactionRepository = new InventoryTransactionRepository();
    private final LotDisposalRepository lotDisposalRepository = new LotDisposalRepository();

    public void recordImport(EntityManager em,
                             ProductLot lot,
                             User actor,
                             String note) {
        if (lot == null || lot.getQtyIn() == null || lot.getQtyIn() <= 0) {
            return;
        }
        createTransaction(em, lot, InventoryTransactionType.IMPORT, lot.getQtyIn(), "LOT", lot.getId(), note, actor);
    }

    public void recordAdjustment(EntityManager em,
                                 ProductLot lot,
                                 int deltaQty,
                                 User actor,
                                 String note) {
        if (lot == null || deltaQty == 0) {
            return;
        }
        createTransaction(em, lot, InventoryTransactionType.ADJUST, deltaQty, "LOT", lot.getId(), note, actor);
    }

    public void recordSaleAllocations(EntityManager em,
                                      OrderItem orderItem,
                                      List<LotConsumption> consumptions,
                                      User actor) {
        if (orderItem == null || consumptions == null || consumptions.isEmpty()) {
            return;
        }

        for (LotConsumption consumption : consumptions) {
            ProductLot lot = em.find(ProductLot.class, consumption.getLotId());
            if (lot == null) {
                throw new IllegalStateException("Lot not found while recording allocation: " + consumption.getLotId());
            }

            OrderItemLotAllocation allocation = new OrderItemLotAllocation();
            allocation.setOrderItem(orderItem);
            allocation.setProductLot(lot);
            allocation.setAllocatedQty(consumption.getQtyTaken());
            allocation.setCreatedAt(LocalDateTime.now());
            allocationRepository.save(em, allocation);

            createTransaction(
                    em,
                    lot,
                    InventoryTransactionType.SALE,
                    -consumption.getQtyTaken(),
                    "ORDER_ITEM",
                    orderItem.getId(),
                    "Order " + orderItem.getOrder().getOrderCode() + " consumed lot #" + lot.getId(),
                    actor
            );
        }
    }

    public LotDisposal recordDisposal(EntityManager em,
                                      ProductLot lot,
                                      int qty,
                                      String reason,
                                      String note,
                                      User actor) {
        if (lot == null) {
            throw new IllegalArgumentException("Lot is required");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Disposed quantity must be greater than 0");
        }

        LotDisposal disposal = new LotDisposal();
        disposal.setProductLot(lot);
        disposal.setDisposedQty(qty);
        disposal.setReason(reason);
        disposal.setNote(note);
        disposal.setDisposedBy(actor);
        disposal.setDisposedAt(LocalDateTime.now());
        lotDisposalRepository.save(em, disposal);
        em.flush();

        createTransaction(
                em,
                lot,
                InventoryTransactionType.DISPOSE,
                -qty,
                "LOT_DISPOSAL",
                disposal.getId(),
                reason + (note == null || note.isBlank() ? "" : " - " + note),
                actor
        );
        return disposal;
    }

    private void createTransaction(EntityManager em,
                                   ProductLot lot,
                                   InventoryTransactionType type,
                                   int qty,
                                   String referenceType,
                                   Long referenceId,
                                   String note,
                                   User actor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductLot(lot);
        transaction.setType(type);
        transaction.setQuantity(qty);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNote(note);
        transaction.setCreatedBy(actor);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(em, transaction);
    }
}

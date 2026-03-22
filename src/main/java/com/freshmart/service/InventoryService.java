package com.freshmart.service;

import com.freshmart.entity.OrderItem;
import com.freshmart.entity.OrderItemLotReservation;
import com.freshmart.entity.ProductLot;
import com.freshmart.exception.InsufficientStockException;
import com.freshmart.repository.OrderItemLotReservationRepository;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.service.dto.LotConsumption;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {

    private final ProductLotRepository lotRepo = new ProductLotRepository();
    private final OrderItemLotReservationRepository reservationRepo = new OrderItemLotReservationRepository();

    public int getAvailableQty(EntityManager em, Long productId, LocalDate today) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (today == null) {
            throw new IllegalArgumentException("Today is required");
        }
        return lotRepo.getAvailableToSellQty(em, productId, today);
    }

    public int getPhysicalQty(EntityManager em, Long productId, LocalDate today) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (today == null) {
            throw new IllegalArgumentException("Today is required");
        }
        return lotRepo.getAvailableQty(em, productId, today);
    }

    public int getReservedQty(EntityManager em, Long productId, LocalDate today) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (today == null) {
            throw new IllegalArgumentException("Today is required");
        }
        return lotRepo.getReservedQty(em, productId, today);
    }

    public List<OrderItemLotReservation> reserveStockFEFO(EntityManager em,
            OrderItem orderItem,
            int qty,
            LocalDate today) {
        if (orderItem == null || orderItem.getProduct() == null || orderItem.getProduct().getId() == null) {
            throw new IllegalArgumentException("Order item / product is required");
        }
        if (today == null) {
            throw new IllegalArgumentException("Today is required");
        }
        if (qty <= 0) {
            return List.of();
        }

        List<ProductLot> lockedLots = lotRepo.findReservableLotsFEFO(em, orderItem.getProduct().getId(), today);
        int lockedAvailable = lockedLots.stream().mapToInt(ProductLot::getAvailableToSell).sum();

        if (lockedAvailable < qty) {
            throw new InsufficientStockException("Not enough ATP stock. Need=" + qty + ", ATP=" + lockedAvailable);
        }

        List<OrderItemLotReservation> created = new ArrayList<>();
        int remaining = qty;

        for (ProductLot lot : lockedLots) {
            if (remaining <= 0) {
                break;
            }

            int canTake = lot.getAvailableToSell();
            if (canTake <= 0) {
                continue;
            }

            int take = Math.min(remaining, canTake);
            int currentReserved = lot.getQtyReserved() == null ? 0 : lot.getQtyReserved();
            lot.setQtyReserved(currentReserved + take);
            em.merge(lot);

            OrderItemLotReservation reservation = new OrderItemLotReservation();
            reservation.setOrderItem(orderItem);
            reservation.setProductLot(lot);
            reservation.setReservedQty(take);
            reservation.setCreatedAt(LocalDateTime.now());
            reservationRepo.save(em, reservation);
            created.add(reservation);

            remaining -= take;
        }

        if (remaining > 0) {
            throw new InsufficientStockException("Reservation failed after locking lots. Remaining=" + remaining);
        }
        return created;
    }

    public long countActiveReservations(EntityManager em, Long orderItemId) {
        if (orderItemId == null) {
            return 0L;
        }
        return reservationRepo.countActiveByOrderItemId(em, orderItemId);
    }

    public void releaseReservations(EntityManager em, Long orderId, String reason) {
        List<OrderItemLotReservation> reservations = reservationRepo.findActiveByOrderId(em, orderId);
        LocalDateTime now = LocalDateTime.now();

        for (OrderItemLotReservation reservation : reservations) {
            ProductLot lot = em.find(ProductLot.class, reservation.getProductLot().getId(),
                    LockModeType.PESSIMISTIC_WRITE);
            if (lot == null) {
                continue;
            }

            int currentReserved = lot.getQtyReserved() == null ? 0 : lot.getQtyReserved();
            lot.setQtyReserved(Math.max(0, currentReserved - reservation.getReservedQty()));
            em.merge(lot);

            reservation.setReleasedAt(now);
            reservation.setReleaseReason(reason);
            reservationRepo.save(em, reservation);
        }
    }

    public List<LotConsumption> consumeReservedStock(EntityManager em, OrderItem orderItem) {
        if (orderItem == null || orderItem.getId() == null) {
            throw new IllegalArgumentException("Order item is required");
        }

        List<OrderItemLotReservation> reservations = reservationRepo.findActiveByOrderItemId(em, orderItem.getId());
        if (reservations.isEmpty()) {
            throw new InsufficientStockException("Order item has no active reservation.");
        }

        int totalReserved = reservations.stream().mapToInt(OrderItemLotReservation::getReservedQty).sum();
        int requiredQty = orderItem.getQuantity() == null ? 0 : orderItem.getQuantity();
        if (totalReserved < requiredQty) {
            throw new InsufficientStockException(
                    "Reserved quantity is not enough. Need=" + requiredQty + ", reserved=" + totalReserved);
        }

        List<LotConsumption> consumed = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int remaining = requiredQty;

        for (OrderItemLotReservation reservation : reservations) {
            if (remaining <= 0) {
                break;
            }

            ProductLot lot = em.find(ProductLot.class, reservation.getProductLot().getId(),
                    LockModeType.PESSIMISTIC_WRITE);
            if (lot == null) {
                throw new IllegalStateException("Reserved lot not found: " + reservation.getProductLot().getId());
            }
            LocalDate today = LocalDate.now();
            if (lot.getExpiryDate() == null || lot.getExpiryDate().isBefore(today)) {
                throw new InsufficientStockException(
                        "Reserved lot expired before order completion. lotId=" + lot.getId());
            }

            int take = Math.min(remaining, reservation.getReservedQty());
            int qtyLeft = lot.getQtyLeft() == null ? 0 : lot.getQtyLeft();
            int qtyReserved = lot.getQtyReserved() == null ? 0 : lot.getQtyReserved();

            if (qtyLeft < take) {
                throw new InsufficientStockException("Lot physical stock changed unexpectedly. lotId=" + lot.getId());
            }
            if (qtyReserved < take) {
                throw new InsufficientStockException("Lot reserved stock changed unexpectedly. lotId=" + lot.getId());
            }

            lot.setQtyLeft(qtyLeft - take);
            lot.setQtyReserved(qtyReserved - take);
            em.merge(lot);

            reservation.setReleasedAt(now);
            reservation.setReleaseReason("CONSUMED_ON_COMPLETE");
            reservationRepo.save(em, reservation);

            consumed.add(new LotConsumption(lot.getId(), take, lot.getExpiryDate()));
            remaining -= take;
        }

        if (remaining > 0) {
            throw new InsufficientStockException("Reserved stock consumption failed. Remaining=" + remaining);
        }
        return consumed;
    }

    /**
     * FEFO (First Expired, First Out) stock deduction.
     * This method MUST be executed inside a transaction.
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
                .mapToInt(ProductLot::getAvailableToSell)
                .sum();

        if (lockedAvailable < qty) {
            throw new InsufficientStockException(
                    "Not enough stock. Need=" + qty + ", available=" + lockedAvailable);
        }

        List<LotConsumption> consumed = new ArrayList<>();
        int remaining = qty;

        for (ProductLot lot : lockedLots) {
            if (remaining <= 0) {
                break;
            }

            int qtyLeft = lot.getQtyLeft() == null ? 0 : lot.getQtyLeft();
            int availableToSell = lot.getAvailableToSell();
            if (availableToSell <= 0) {
                continue;
            }

            int take = Math.min(remaining, availableToSell);
            lot.setQtyLeft(qtyLeft - take);
            em.merge(lot);

            consumed.add(new LotConsumption(lot.getId(), take, lot.getExpiryDate()));
            remaining -= take;
        }

        if (remaining > 0) {
            throw new InsufficientStockException(
                    "Stock deduction failed after locking lots. Remaining=" + remaining);
        }

        return consumed;
    }
}

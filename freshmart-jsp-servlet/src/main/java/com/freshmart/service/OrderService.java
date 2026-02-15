package com.freshmart.service;

import com.freshmart.entity.Order;
import com.freshmart.entity.OrderItem;
import com.freshmart.entity.Product;
import com.freshmart.entity.User;
import com.freshmart.enums.OrderStatus;
import com.freshmart.enums.OrderType;
import com.freshmart.enums.PaymentMethod;
import com.freshmart.repository.OrderRepository;
import com.freshmart.service.dto.ItemRequest;
import com.freshmart.util.CodeGenerator;
import com.freshmart.util.JpaExecutor;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderService {

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderRepository orderRepo = new OrderRepository();
    private final InventoryService inventoryService = new InventoryService();
    private final RevenueService revenueService = new RevenueService();

    public Order createSellerWalkInOrder(Long sellerUserId,
                                         PaymentMethod paymentMethod,
                                         List<ItemRequest> items,
                                         boolean completeNow) {
        return executor.execute(em -> {
            User seller = requireUser(em, sellerUserId);

            Order order = new Order();
            order.setOrderCode(CodeGenerator.orderCode());
            order.setCreatedBy(seller);
            order.setType(OrderType.WALK_IN);
            order.setPaymentMethod(paymentMethod);

            if (completeNow) {
                order.setStatus(OrderStatus.COMPLETED);
                order.setCompletedAt(LocalDateTime.now());
            } else {
                order.setStatus(OrderStatus.PENDING);
            }

            LocalDate today = LocalDate.now();

            for (ItemRequest req : items) {
                if (req.getQuantity() <= 0) continue;

                Product p = em.find(Product.class, req.getProductId());
                if (p == null) {
                    throw new IllegalArgumentException("Product not found: " + req.getProductId());
                }

                // If completed, deduct inventory FEFO right away
                if (completeNow) {
                    inventoryService.consumeStockFEFO(em, p.getId(), req.getQuantity(), today);
                }

                OrderItem item = new OrderItem(p, req.getQuantity(), p.getSellPrice());
                order.addItem(item);
            }

            // compute total
            BigDecimal total = order.getItems().stream()
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);

            orderRepo.save(em, order);

            // update revenue_daily only when COMPLETED
            if (completeNow) {
                revenueService.addRevenue(em, order.getCompletedAt().toLocalDate(), total);
            }

            return order;
        });
    }

    public Order completeOrder(Long orderId) {
        return executor.execute(em -> {
            Order order = orderRepo.findById(em, orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            if (order.getStatus() == OrderStatus.COMPLETED) return order;
            if (order.getStatus() == OrderStatus.CANCELED) {
                throw new IllegalStateException("Cannot complete a canceled order.");
            }

            LocalDate today = LocalDate.now();

            // Deduct inventory FEFO per item
            for (OrderItem item : order.getItems()) {
                inventoryService.consumeStockFEFO(em, item.getProduct().getId(), item.getQuantity(), today);
            }

            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());

            // update revenue
            revenueService.addRevenue(em, order.getCompletedAt().toLocalDate(), order.getTotalAmount());

            return orderRepo.save(em, order);
        });
    }

    private User requireUser(EntityManager em, Long id) {
        User u = em.find(User.class, id);
        if (u == null) throw new IllegalArgumentException("User not found: " + id);
        return u;
    }
}

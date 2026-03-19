package com.freshmart.service;

import com.freshmart.entity.CartItem;
import com.freshmart.entity.Order;
import com.freshmart.entity.OrderItem;
import com.freshmart.entity.Product;
import com.freshmart.entity.User;
import com.freshmart.enums.OrderStatus;
import com.freshmart.enums.OrderType;
import com.freshmart.enums.PaymentMethod;
import com.freshmart.repository.CartRepository;
import com.freshmart.repository.OrderRepository;
import com.freshmart.service.dto.ItemRequest;
import com.freshmart.service.dto.LotConsumption;
import com.freshmart.util.CodeGenerator;
import com.freshmart.util.JpaExecutor;
import com.freshmart.util.OrderStatusTransition;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OrderService {

    private final JpaExecutor executor = new JpaExecutor();
    private final OrderRepository orderRepo = new OrderRepository();
    private final InventoryService inventoryService = new InventoryService();
    private final RevenueService revenueService = new RevenueService();
    private final InventoryAuditService inventoryAuditService = new InventoryAuditService();

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
            order.setStatus(completeNow ? OrderStatus.COMPLETED : OrderStatus.PENDING);
            if (completeNow) {
                order.setCompletedAt(LocalDateTime.now());
            }

            for (ItemRequest req : items) {
                if (req == null || req.getQuantity() <= 0) {
                    continue;
                }

                Product product = em.find(Product.class, req.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + req.getProductId());
                }
                if (!product.isActive()) {
                    throw new IllegalStateException("Product is inactive: " + product.getName());
                }

                OrderItem item = new OrderItem(product, req.getQuantity(), product.getSellPrice());
                order.addItem(item);
            }

            if (order.getItems().isEmpty()) {
                throw new IllegalArgumentException("Order must contain at least one valid item.");
            }

            BigDecimal total = order.getItems().stream()
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);

            orderRepo.save(em, order);
            em.flush();

            if (completeNow) {
                consumeAndTraceOrderItems(em, order, seller);
                revenueService.addRevenue(em, order.getCompletedAt().toLocalDate(), total);
            }

            return order;
        });
    }

    public Order findById(Long id) {
        return executor.execute(em -> orderRepo.findById(em, id).orElse(null));
    }

    public Order completeOrder(Long orderId) {
        return completeOrder(orderId, null);
    }

    public Order completeOrder(Long orderId, Long actorUserId) {
        return executor.execute(em -> {
            Order order = orderRepo.findByIdForUpdate(em, orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            if (order.getStatus() == OrderStatus.COMPLETED) {
                return order;
            }
            if (order.getStatus() == OrderStatus.CANCELED) {
                throw new IllegalStateException("Cannot complete a canceled order.");
            }

            User actor = resolveUser(em, actorUserId);
            finalizeOrder(em, order, actor);
            return order;
        });
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        return updateStatus(orderId, newStatus, null);
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus, Long actorUserId) {

        return executor.execute(em -> {
            Order order = orderRepo.findByIdForUpdate(em, orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            OrderStatus current = order.getStatus();

            if (current == OrderStatus.COMPLETED || current == OrderStatus.CANCELED) {
                throw new IllegalStateException("Order already finished: " + current);
            }

            if (!OrderStatusTransition.isAllowed(current, newStatus)) {
                throw new IllegalStateException(
                        "Invalid status transition (workflow): " + current + " -> " + newStatus);
            }

            if (newStatus == OrderStatus.COMPLETED) {
                finalizeOrder(em, order, resolveUser(em, actorUserId));
            } else {
                order.setStatus(newStatus);
                orderRepo.save(em, order);
            }

            return order;
        });
    }

    public void updateOrderStatus(Long orderId, OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("Target status is required.");
        }
        updateStatus(orderId, targetStatus);
    }

    public Order createCustomerOrder(Long customerId) {

        return executor.execute(em -> {
            User customer = requireUser(em, customerId);

            CartRepository cartRepo = new CartRepository();
            List<CartItem> cartItems = cartRepo.findItemsByUserId(em, customerId);
            if (cartItems.isEmpty()) {
                throw new IllegalStateException("Cart is empty");
            }

            Order order = new Order();
            order.setOrderCode(CodeGenerator.orderCode());
            order.setCustomer(customer);
            order.setCreatedBy(null);
            order.setType(OrderType.ONLINE);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentMethod(PaymentMethod.COD);
            order.setCreatedAt(LocalDateTime.now());

            LocalDate today = LocalDate.now();
            for (CartItem ci : cartItems) {
                Product product = em.find(Product.class, ci.getProduct().getId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + ci.getProduct().getId());
                }
                if (!product.isActive()) {
                    throw new IllegalStateException("Product is inactive: " + product.getName());
                }

                int availableQty = inventoryService.getAvailableQty(em, product.getId(), today);
                if (ci.getQuantity() > availableQty) {
                    throw new IllegalStateException("Not enough stock for product: " + product.getName());
                }

                order.addItem(new OrderItem(product, ci.getQuantity(), product.getSellPrice()));
            }

            BigDecimal total = order.getItems().stream()
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);
            orderRepo.save(em, order);

            for (CartItem ci : cartItems) {
                em.remove(ci);
            }
            return order;
        });
    }

    private void finalizeOrder(EntityManager em, Order order, User actor) {
        consumeAndTraceOrderItems(em, order, actor);
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepo.save(em, order);
        revenueService.addRevenue(em, order.getCompletedAt().toLocalDate(), order.getTotalAmount());
    }

    private void consumeAndTraceOrderItems(EntityManager em, Order order, User actor) {
        LocalDate today = LocalDate.now();
        List<OrderItem> sortedItems = new ArrayList<>(order.getItems());
        sortedItems.sort(Comparator.comparing(item -> item.getProduct().getId()));

        for (OrderItem item : sortedItems) {
            List<LotConsumption> consumptions = inventoryService.consumeStockFEFO(
                    em,
                    item.getProduct().getId(),
                    item.getQuantity(),
                    today
            );
            inventoryAuditService.recordSaleAllocations(em, item, consumptions, actor);
        }
    }

    private User requireUser(EntityManager em, Long id) {
        User user = em.find(User.class, id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        return user;
    }

    private User resolveUser(EntityManager em, Long id) {
        if (id == null) {
            return null;
        }
        return requireUser(em, id);
    }
}

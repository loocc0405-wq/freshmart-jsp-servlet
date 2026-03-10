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

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ===== ADDED FOR CUSTOMER CHECKOUT =====
import com.freshmart.entity.CartItem;
import com.freshmart.repository.CartRepository;
// ===== END ADDED =====

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
                if (!p.isActive()) {
                    throw new IllegalStateException("Product is inactive: " + p.getName());
                }

                if (completeNow) {
                    inventoryService.consumeStockFEFO(em, p.getId(), req.getQuantity(), today);
                }

                OrderItem item = new OrderItem(p, req.getQuantity(), p.getSellPrice());
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

            for (OrderItem item : order.getItems()) {
                
                inventoryService.consumeStockFEFO(em,
                        item.getProduct().getId(),
                        item.getQuantity(),
                        today);
            }

            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());

            revenueService.addRevenue(em,
                    order.getCompletedAt().toLocalDate(),
                    order.getTotalAmount());

            return orderRepo.save(em, order);
        });
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus) {

        return executor.execute(em -> {

            Order order = orderRepo.findById(em, orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            OrderStatus current = order.getStatus();

            // không cho thay đổi khi đã kết thúc
            if (current == OrderStatus.COMPLETED || current == OrderStatus.CANCELED) {
                throw new IllegalStateException("Order already finished: " + current);
            }

            if (!isValidTransition(current, newStatus)) {
                throw new IllegalStateException(
                        "Invalid status transition: " + current + " -> " + newStatus);
            }

            // SHIPPING -> COMPLETED cần trừ tồn và cộng revenue
            if (newStatus == OrderStatus.COMPLETED) {

                LocalDate today = LocalDate.now();

                for (OrderItem item : order.getItems()) {
                    inventoryService.consumeStockFEFO(
                            em,
                            item.getProduct().getId(),
                            item.getQuantity(),
                            today
                    );
                }

                order.setCompletedAt(LocalDateTime.now());

                revenueService.addRevenue(
                        em,
                        order.getCompletedAt().toLocalDate(),
                        order.getTotalAmount()
                );
            }

            order.setStatus(newStatus);

            return orderRepo.save(em, order);
        });
    }

    // ===== ADDED FOR CUSTOMER CHECKOUT =====
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

            // đúng quan hệ business
            order.setCustomer(customer);
            order.setCreatedBy(null);

            order.setType(OrderType.ONLINE);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentMethod(PaymentMethod.COD);
            order.setCreatedAt(LocalDateTime.now());

            LocalDate today = LocalDate.now();

            for (CartItem ci : cartItems) {

                Product p = em.find(Product.class, ci.getProduct().getId());

                if (p == null) {
                    throw new IllegalArgumentException("Product not found: " + ci.getProduct().getId());
                }

                if (!p.isActive()) {
                    throw new IllegalStateException("Product is inactive: " + p.getName());
                }

                int availableQty = inventoryService.getAvailableQty(em, p.getId(), today);
                if (ci.getQuantity() > availableQty) {
                    throw new IllegalStateException("Not enough stock for product: " + p.getName());
                }

                OrderItem item = new OrderItem(
                        p,
                        ci.getQuantity(),
                        p.getSellPrice()
                );

                order.addItem(item);
            }

            BigDecimal total = order.getItems().stream()
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setTotalAmount(total);

            orderRepo.save(em, order);

            // chỉ clear cart, KHÔNG trừ tồn, KHÔNG cộng revenue ở đây
            for (CartItem ci : cartItems) {
                em.remove(ci);
            }

            return order;
        });
    }
    // ===== END ADDED =====

    private User requireUser(EntityManager em, Long id) {
        User u = em.find(User.class, id);
        if (u == null) throw new IllegalArgumentException("User not found: " + id);
        return u;
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {

        switch (from) {

            case PENDING:
                return to == OrderStatus.PROCESSING
                        || to == OrderStatus.CANCELED;

            case PROCESSING:
                return to == OrderStatus.SHIPPING
                        || to == OrderStatus.CANCELED;

            case SHIPPING:
                return to == OrderStatus.COMPLETED;

            default:
                return false;
        }
    }
}
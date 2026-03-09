package com.freshmart.service;

import com.freshmart.entity.*;
import com.freshmart.repository.*;
import com.freshmart.util.JpaExecutor;

import java.time.LocalDate;
import java.util.List;

public class CartService {

    private final JpaExecutor executor = new JpaExecutor();

    private final CartRepository cartRepo = new CartRepository();
    private final CartItemRepository cartItemRepo = new CartItemRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final ProductLotRepository lotRepo = new ProductLotRepository();
    private final InventoryService inventoryService = new InventoryService();

    // =====================================================
    // HELPER: GET TOTAL STOCK
    // =====================================================
    private int getAvailableStock(jakarta.persistence.EntityManager em, Long productId) {

        List<ProductLot> lots = lotRepo.findAvailableLotsFEFO(
                em,
                productId,
                LocalDate.now()
        );

        int total = 0;

        for (ProductLot lot : lots) {
            total += lot.getQtyLeft();
        }

        return total;
    }

    // ===============================
    // GET CART ITEMS
    // ===============================
    public List<CartItem> getCartItems(Long userId) {
        return executor.execute(em -> {
            Cart cart = cartRepo.findByUserId(em, userId)
                    .orElse(null);
            if (cart == null) return List.of();
            return cartItemRepo.findByCartId(em, cart.getId());
        });
    }

    // ===============================
    // ADD TO CART
    // ===============================
    public void addToCart(Long userId, Long productId, int qty) {

        executor.executeVoid(em -> {

            // ------------------------------
            // VALIDATE QTY
            // ------------------------------
            if (qty <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }

            int stock = getAvailableStock(em, productId);

            if (stock <= 0) {
                throw new RuntimeException("Product out of stock");
            }

            Cart cart = cartRepo.findByUserId(em, userId)
                    .orElseGet(() -> cartRepo.createCart(em, userId));

            Product product = productRepo.findById(em, productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            CartItem item = cartItemRepo
                    .findByCartAndProduct(em, cart.getId(), productId)
                    .orElse(null);

            if (item == null) {

                // CHECK STOCK BEFORE ADD
                if (qty > stock) {
                    throw new RuntimeException("Not enough stock");
                }

                item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(qty);
                em.persist(item);

            } else {
int newQty = item.getQuantity() + qty;

                if (newQty > stock) {
                    throw new RuntimeException("Not enough stock");
                }

                item.setQuantity(newQty);
                em.merge(item);
            }
        });
    }

    // ===============================
    // UPDATE QUANTITY
    // ===============================
    public void updateQuantity(Long userId, Long productId, int qty) {

        executor.executeVoid(em -> {

            Cart cart = cartRepo.findByUserId(em, userId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            CartItem item = cartItemRepo
                    .findByCartAndProduct(em, cart.getId(), productId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (qty <= 0) {
                em.remove(item);
            } else {

                // ------------------------------
                // CHECK STOCK
                // ------------------------------
                int stock = getAvailableStock(em, productId);

                if (qty > stock) {
                    throw new RuntimeException("Not enough stock");
                }

                item.setQuantity(qty);
                em.merge(item);
            }
        });
    }

    // ===============================
    // REMOVE ITEM
    // ===============================
    public void removeItem(Long userId, Long productId) {

        executor.executeVoid(em -> {
            Cart cart = cartRepo.findByUserId(em, userId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            cartItemRepo
                    .findByCartAndProduct(em, cart.getId(), productId)
                    .ifPresent(em::remove);
        });
    }

    // ===============================
    // MERGE CART WHEN LOGIN
    // ===============================
    public void mergeCart(Long userId, List<CartItem> sessionItems) {

        if (sessionItems == null || sessionItems.isEmpty()) {
            return;
        }

        executor.executeVoid(em -> {

            Cart cart = cartRepo.findByUserId(em, userId)
                    .orElseGet(() -> cartRepo.createCart(em, userId));

            for (CartItem sessionItem : sessionItems) {

                Long productId = sessionItem.getProduct().getId();
                int qty = sessionItem.getQuantity();

                int stock = getAvailableStock(em, productId);

                if (qty > stock) {
                    qty = stock;
                }

                if (qty <= 0) continue;

                CartItem item = cartItemRepo
                        .findByCartAndProduct(em, cart.getId(), productId)
                        .orElse(null);

                if (item == null) {

                    Product product = productRepo.findById(em, productId)
                            .orElseThrow();

                    CartItem newItem = new CartItem();
newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(qty);

                    em.persist(newItem);

                } else {

                    int newQty = item.getQuantity() + qty;

                    if (newQty > stock) {
                        newQty = stock;
                    }

                    item.setQuantity(newQty);
                    em.merge(item);
                }
            }
        });
    }

    // ===============================
// CHECKOUT (FEFO)
// ===============================
public void checkout(Long userId) {

    executor.executeVoid(em -> {

        Cart cart = cartRepo.findByUserId(em, userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> items = cartItemRepo.findByCartId(em, cart.getId());

        // ===============================
        // [ADDED] CHECK CART EMPTY
        // ===============================
        if (items.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // ===============================
        // [ADDED] VALIDATE STOCK FIRST
        // ===============================
        for (CartItem ci : items) {

            Long productId = ci.getProduct().getId();
            int qty = ci.getQuantity();

            int stock = getAvailableStock(em, productId);

            if (qty > stock) {
                throw new RuntimeException(
                        "Not enough stock for product: "
                                + ci.getProduct().getName()
                );
            }
        }

        // ===============================
        // CONSUME STOCK (FEFO)
        // ===============================
        for (CartItem ci : items) {

            inventoryService.consumeStockFEFO(
                    em,
                    ci.getProduct().getId(),
                    ci.getQuantity(),
                    LocalDate.now()
            );
        }

        // ===============================
        // CLEAR CART
        // ===============================
        for (CartItem ci : items) {
            em.remove(ci);
        }

        // ===============================
        // [ADDED] FLUSH CHANGES
        // ===============================
        em.flush();

    });
}
   
}
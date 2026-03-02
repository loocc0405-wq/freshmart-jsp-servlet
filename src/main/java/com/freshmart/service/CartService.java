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

            Cart cart = cartRepo.findByUserId(em, userId)
                    .orElseGet(() -> cartRepo.createCart(em, userId));

            Product product = productRepo.findById(em, productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            CartItem item = cartItemRepo
                    .findByCartAndProduct(em, cart.getId(), productId)
                    .orElse(null);

            if (item == null) {
                item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(qty);
                em.persist(item);
            } else {
                item.setQuantity(item.getQuantity() + qty);
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
    // CHECKOUT (FEFO)
    // ===============================
    public void checkout(Long userId) {

        executor.executeVoid(em -> {

            Cart cart = cartRepo.findByUserId(em, userId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            List<CartItem> items = cartItemRepo.findByCartId(em, cart.getId());

            for (CartItem ci : items) {

                int qtyNeed = ci.getQuantity();

                List<ProductLot> lots = lotRepo.findAvailableLotsFEFO(
                        em,
                        ci.getProduct().getId(),
                        LocalDate.now()
                );

                for (ProductLot lot : lots) {

                    if (qtyNeed <= 0) break;

                    int take = Math.min(qtyNeed, lot.getQtyLeft());
                    lot.setQtyLeft(lot.getQtyLeft() - take);
                    em.merge(lot);
                    qtyNeed -= take;
                }

                if (qtyNeed > 0) {
                    throw new RuntimeException("Not enough stock");
                }
            }

            // Clear cart
            for (CartItem ci : items) {
                em.remove(ci);
            }
        });
    }
}
package com.freshmart.repository;

import com.freshmart.entity.CartItem;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class CartItemRepository {

    public Optional<CartItem> findByCartAndProduct(EntityManager em, Long cartId, Long productId) {
        return em.createQuery(
                "SELECT ci FROM CartItem ci " +
                "WHERE ci.cart.id = :cid AND ci.product.id = :pid",
                CartItem.class
        )
        .setParameter("cid", cartId)
        .setParameter("pid", productId)
        .getResultStream()
        .findFirst();
    }

    public List<CartItem> findByCartId(EntityManager em, Long cartId) {
        return em.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.cart.id = :cid",
                CartItem.class
        )
        .setParameter("cid", cartId)
        .getResultList();
    }
}
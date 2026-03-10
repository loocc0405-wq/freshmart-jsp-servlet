package com.freshmart.repository;

import com.freshmart.entity.Cart;
import com.freshmart.entity.CartItem;
import com.freshmart.entity.User;

import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.List;

public class CartRepository {

    public Optional<Cart> findByUserId(EntityManager em, Long userId) {
        return em.createQuery(
                "SELECT c FROM Cart c WHERE c.user.id = :uid",
                Cart.class
        )
        .setParameter("uid", userId)
        .getResultStream()
        .findFirst();
    }

    public Cart createCart(EntityManager em, Long userId) {

        User user = em.find(User.class, userId);

        Cart cart = new Cart();
        cart.setUser(user);

        em.persist(cart);

        return cart;
    }

    public List<CartItem> findItemsByUserId(EntityManager em, Long userId) {

        return em.createQuery(
                "SELECT c FROM CartItem c WHERE c.cart.user.id = :uid",
                CartItem.class
        )
        .setParameter("uid", userId)
        .getResultList();
    }
}
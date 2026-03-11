package com.freshmart.util;

import com.freshmart.entity.CartItem;
import com.freshmart.entity.Product;

import jakarta.servlet.http.HttpSession;
import java.util.*;

public class GuestCartUtil {

    public static final String SESSION_GUEST_CART = "GUEST_CART_ITEMS";

    @SuppressWarnings("unchecked")
    public static List<CartItem> getGuestCart(HttpSession session) {

        Object obj = session.getAttribute(SESSION_GUEST_CART);

        if (obj instanceof List<?>) {
            return (List<CartItem>) obj;
        }

        List<CartItem> cart = new ArrayList<>();
        session.setAttribute(SESSION_GUEST_CART, cart);

        return cart;
    }

    public static void addItem(HttpSession session, Product product, int qty) {

        List<CartItem> cart = getGuestCart(session);

        for (CartItem item : cart) {

            if (item.getProduct().getId().equals(product.getId())) {

                item.setQuantity(item.getQuantity() + qty);
                return;
            }
        }

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(qty);

        cart.add(item);
    }

    public static void updateItem(HttpSession session, Long productId, int qty) {

        List<CartItem> cart = getGuestCart(session);

        Iterator<CartItem> it = cart.iterator();

        while (it.hasNext()) {

            CartItem item = it.next();

            if (item.getProduct().getId().equals(productId)) {

                if (qty <= 0) {
                    it.remove();
                } else {
                    item.setQuantity(qty);
                }

                return;
            }
        }
    }

    public static void removeItem(HttpSession session, Long productId) {

        List<CartItem> cart = getGuestCart(session);

        cart.removeIf(i -> i.getProduct().getId().equals(productId));
    }

}
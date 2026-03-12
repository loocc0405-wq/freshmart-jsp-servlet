package com.freshmart.service;

import com.freshmart.entity.CartItem;
import com.freshmart.util.JpaExecutor;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CartServiceEdgeCaseTest {

    private final JpaExecutor executor = new JpaExecutor();
    private final CartService cartService = new CartService();

    private String runKey;
    private Long TEST_USER_ID;

    @BeforeEach
    void setup() {

        runKey = "CART_TEST_" + UUID.randomUUID().toString().substring(0,6);

        // tạo user test đúng schema User entity
        TEST_USER_ID = executor.execute(em -> {

            String username = runKey + "_USER";

            em.createNativeQuery(
                    "INSERT INTO users(username, email, password_hash, role, tier, active) " +
                    "VALUES(:u, :e,'123','CUSTOMER','FREE',1)"
            )
            .setParameter("u", username)
            .setParameter("e", username.toLowerCase() + "@test.local")
            .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                    "SELECT TOP 1 id FROM users WHERE username = :u ORDER BY id DESC"
            )
            .setParameter("u", username)
            .getSingleResult();

            return id.longValue();
        });
    }

    @AfterEach
    void cleanup() {

        executor.executeVoid(em -> {

            em.createNativeQuery(
                    "DELETE FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE user_id = :uid)"
            )
            .setParameter("uid", TEST_USER_ID)
            .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM carts WHERE user_id = :uid"
            )
            .setParameter("uid", TEST_USER_ID)
            .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM product_lots WHERE product_id IN (" +
                            "SELECT id FROM products WHERE name LIKE :prefix)"
            )
            .setParameter("prefix", runKey + "%")
            .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM products WHERE name LIKE :prefix"
            )
            .setParameter("prefix", runKey + "%")
            .executeUpdate();

            em.createNativeQuery(
                    "DELETE FROM users WHERE id = :uid"
            )
            .setParameter("uid", TEST_USER_ID)
            .executeUpdate();
        });
    }

    // ===============================
    // add to cart normal
    // ===============================

    @Test
    void addToCart_shouldWorkNormally() {

        Long productId = insertProductWithStock();

        assertDoesNotThrow(() ->
                cartService.addToCart(TEST_USER_ID, productId, 1)
        );
    }

    // ===============================
    // negative quantity
    // ===============================

    @Test
    void addToCart_negativeQuantity_shouldThrow() {

        Long productId = insertProductWithStock();

        assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(TEST_USER_ID, productId, -5)
        );
    }

    // ===============================
    // remove item not exist
    // ===============================

    @Test
    void removeItem_notExist_shouldThrowCartNotFound() {

        Long productId = insertProductWithStock();

        assertThrows(
                RuntimeException.class,
                () -> cartService.removeItem(TEST_USER_ID, productId)
        );
    }

    // ===============================
    // update quantity = 0
    // ===============================

    @Test
    void updateQuantity_zero_shouldRemoveItem() {

        Long productId = insertProductWithStock();

        cartService.addToCart(TEST_USER_ID, productId, 2);

        cartService.updateQuantity(TEST_USER_ID, productId, 0);

        List<CartItem> items = cartService.getCartItems(TEST_USER_ID);

        assertTrue(items.isEmpty());
    }

    // ===============================
    // large quantity
    // ===============================

    @Test
    void addLargeQuantity_shouldNotCrash() {

        Long productId = insertProductWithStock();

        assertDoesNotThrow(() ->
                cartService.addToCart(TEST_USER_ID, productId, 100)
        );
    }

    // ===============================
    // HELPER
    // ===============================

    private Long insertProductWithStock() {

        return executor.execute(em -> {

            String name = runKey + "_PRODUCT";

            em.createNativeQuery(
                    "INSERT INTO products(name, category, unit, sell_price, active) " +
                    "VALUES (:name,'TEST','pcs',10000,1)"
            )
            .setParameter("name", name)
            .executeUpdate();

            Number id = (Number) em.createNativeQuery(
                    "SELECT TOP 1 id FROM products WHERE name = :name ORDER BY id DESC"
            )
            .setParameter("name", name)
            .getSingleResult();

            Long productId = id.longValue();

            em.createNativeQuery(
                    "INSERT INTO product_lots(product_id, supplier_id, import_date, expiry_date, qty_in, qty_left, import_price) " +
                    "VALUES (:pid,NULL,GETDATE(),DATEADD(day,30,GETDATE()),500,500,5000)"
            )
            .setParameter("pid", productId)
            .executeUpdate();

            return productId;
        });
    }
}
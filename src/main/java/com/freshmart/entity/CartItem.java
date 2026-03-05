package com.freshmart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_cart_items_cart_product",
                        columnNames = {"cart_id", "product_id"}
                )
        })
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giữ LAZY vì không dùng cart trong JSP
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id",
            foreignKey = @ForeignKey(name = "fk_cart_items_cart"))
    private Cart cart;

    // 🔥 SỬA Ở ĐÂY: đổi LAZY -> EAGER
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id",
            foreignKey = @ForeignKey(name = "fk_cart_items_product"))
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    public CartItem() {}

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Transient
    public Integer getQty() {
        return getQuantity();
    }

    public void setQty(Integer qty) {
        setQuantity(qty);
    }

    @Transient
    public BigDecimal getLineTotal() {
        BigDecimal price = (product == null || product.getSellPrice() == null)
                ? BigDecimal.ZERO
                : product.getSellPrice();
        int qty = (quantity == null) ? 0 : quantity;
        return price.multiply(BigDecimal.valueOf(qty));
    }
}
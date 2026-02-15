package com.freshmart.web.servlet.seller.dto;

import com.freshmart.entity.Product;

import java.math.BigDecimal;

public class PosLine {
    private final Product product;
    private final int quantity;
    private final BigDecimal lineTotal;

    public PosLine(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.lineTotal = product.getSellPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}

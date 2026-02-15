package com.freshmart.service.dto;

public class ItemRequest {
    private final Long productId;
    private final int quantity;

    public ItemRequest(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}

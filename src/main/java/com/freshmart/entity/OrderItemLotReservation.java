package com.freshmart.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_item_lot_reservations", indexes = {
        @Index(name = "idx_oir_order_item", columnList = "order_item_id"),
        @Index(name = "idx_oir_product_lot", columnList = "product_lot_id")
})
public class OrderItemLotReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_oir_order_item"))
    private OrderItem orderItem;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_lot_id", nullable = false, foreignKey = @ForeignKey(name = "fk_oir_product_lot"))
    private ProductLot productLot;

    @Column(name = "reserved_qty", nullable = false)
    private Integer reservedQty;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "release_reason", length = 255)
    private String releaseReason;

    public Long getId() {
        return id;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public ProductLot getProductLot() {
        return productLot;
    }

    public void setProductLot(ProductLot productLot) {
        this.productLot = productLot;
    }

    public Integer getReservedQty() {
        return reservedQty;
    }

    public void setReservedQty(Integer reservedQty) {
        this.reservedQty = reservedQty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public String getReleaseReason() {
        return releaseReason;
    }

    public void setReleaseReason(String releaseReason) {
        this.releaseReason = releaseReason;
    }

    @Transient
    public boolean isActive() {
        return releasedAt == null;
    }
}

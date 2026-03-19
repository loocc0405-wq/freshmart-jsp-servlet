package com.freshmart.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lot_disposals",
        indexes = {
                @Index(name = "idx_lot_disposals_lot", columnList = "product_lot_id"),
                @Index(name = "idx_lot_disposals_time", columnList = "disposed_at")
        })
public class LotDisposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_lot_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lot_disposals_lot"))
    private ProductLot productLot;

    @Column(name = "disposed_qty", nullable = false)
    private Integer disposedQty;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disposed_by", foreignKey = @ForeignKey(name = "fk_lot_disposals_user"))
    private User disposedBy;

    @Column(name = "disposed_at", nullable = false)
    private LocalDateTime disposedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public ProductLot getProductLot() {
        return productLot;
    }

    public void setProductLot(ProductLot productLot) {
        this.productLot = productLot;
    }

    public Integer getDisposedQty() {
        return disposedQty;
    }

    public void setDisposedQty(Integer disposedQty) {
        this.disposedQty = disposedQty;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getDisposedBy() {
        return disposedBy;
    }

    public void setDisposedBy(User disposedBy) {
        this.disposedBy = disposedBy;
    }

    public LocalDateTime getDisposedAt() {
        return disposedAt;
    }

    public void setDisposedAt(LocalDateTime disposedAt) {
        this.disposedAt = disposedAt;
    }
}

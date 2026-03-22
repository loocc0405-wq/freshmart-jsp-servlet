package com.freshmart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product_lots", indexes = {
        @Index(name = "idx_lots_product_expiry", columnList = "product_id, expiry_date")
})
public class ProductLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_lots_product"))
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name = "fk_lots_supplier"))
    private Supplier supplier;

    @Column(name = "import_date", nullable = false)
    private LocalDate importDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "qty_in", nullable = false)
    private Integer qtyIn;

    @Column(name = "qty_left", nullable = false)
    private Integer qtyLeft;

    @Column(name = "qty_reserved", nullable = false)
    private Integer qtyReserved = 0;

    @Column(name = "import_price", precision = 18, scale = 2)
    private BigDecimal importPrice = BigDecimal.ZERO;

    public ProductLot() {
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getQtyIn() {
        return qtyIn;
    }

    public void setQtyIn(Integer qtyIn) {
        this.qtyIn = qtyIn;
    }

    public Integer getQtyLeft() {
        return qtyLeft;
    }

    public void setQtyLeft(Integer qtyLeft) {
        this.qtyLeft = qtyLeft;
    }

    public Integer getQtyReserved() {
        return qtyReserved;
    }

    public void setQtyReserved(Integer qtyReserved) {
        this.qtyReserved = qtyReserved;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    @Transient
    public int getAvailableToSell() {
        int left = qtyLeft == null ? 0 : qtyLeft;
        int reserved = qtyReserved == null ? 0 : qtyReserved;
        return Math.max(0, left - reserved);
    }
}

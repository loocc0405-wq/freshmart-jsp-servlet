package com.freshmart.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_products_category", columnList = "category")
        })
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    private String unit;

    @Column(name = "sell_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal sellPrice = BigDecimal.ZERO;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Product() {}

    public Product(String name, BigDecimal sellPrice) {
        this.name = name;
        this.sellPrice = sellPrice;
    }

    // Getters / setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.Supplier;
import com.freshmart.entity.User;
import com.freshmart.enums.Role;
import com.freshmart.enums.Tier;
import com.freshmart.repository.UserRepository;
import com.freshmart.util.JpaExecutor;
import com.freshmart.util.PasswordUtil;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class BootstrapService {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepo = new UserRepository();

    public void ensureDevData() {
        executor.executeVoid(this::seedIfEmpty);
    }

    private void seedIfEmpty(EntityManager em) {
        // make sure schema is compatible (add missing columns manually)
        ensureActiveColumn(em);

        long userCount = userRepo.count(em);
        if (userCount == 0) {
            User admin = new User("admin", "admin@freshmart.local", PasswordUtil.hash("admin123"), Role.ADMIN);
            admin.setTier(Tier.PRO);
            admin.setExpiredDate(LocalDate.now().plusYears(5));

            User staff = new User("staff", "staff@freshmart.local", PasswordUtil.hash("staff123"), Role.STAFF);
            staff.setTier(Tier.FREE);

            User seller = new User("seller", "seller@freshmart.local", PasswordUtil.hash("seller123"), Role.SELLER);
            seller.setTier(Tier.FREE);

            User customer = new User("customer", "customer@freshmart.local", PasswordUtil.hash("customer123"), Role.CUSTOMER);
            customer.setTier(Tier.PRO);
            customer.setExpiredDate(LocalDate.now().plusDays(30));

            em.persist(admin);
            em.persist(staff);
            em.persist(seller);
            em.persist(customer);
        }

        // Seed catalog data for local development (idempotent by product name)
        ensureCatalogData(em);
    }

    private void ensureCatalogData(EntityManager em) {
        Supplier sup = ensureSupplier(em);

        List<SeedProduct> items = Arrays.asList(
                // ===== Rau củ =====
                new SeedProduct("Rau muống", "Rau củ", "bó", new BigDecimal("15000"), 50, 7),
                new SeedProduct("Cải thìa", "Rau củ", "bó", new BigDecimal("18000"), 40, 7),
                new SeedProduct("Bông cải xanh", "Rau củ", "kg", new BigDecimal("35000"), 30, 10),
                new SeedProduct("Cà rốt", "Rau củ", "kg", new BigDecimal("25000"), 40, 14),
                new SeedProduct("Khoai tây", "Rau củ", "kg", new BigDecimal("32000"), 40, 21),
                new SeedProduct("Hành lá", "Rau củ", "bó", new BigDecimal("12000"), 40, 7),
                new SeedProduct("Nấm kim châm", "Rau củ", "gói", new BigDecimal("18000"), 30, 10),
                new SeedProduct("Dưa leo", "Rau củ", "kg", new BigDecimal("22000"), 40, 10),

                // ===== Thịt =====
                new SeedProduct("Thịt heo", "Thịt", "kg", new BigDecimal("120000"), 60, 5),
                new SeedProduct("Ba rọi heo", "Thịt", "kg", new BigDecimal("170000"), 50, 5),
                new SeedProduct("Sườn non", "Thịt", "kg", new BigDecimal("190000"), 40, 5),
                new SeedProduct("Thịt bò", "Thịt", "kg", new BigDecimal("280000"), 40, 5),
                new SeedProduct("Thịt gà ta", "Thịt", "kg", new BigDecimal("160000"), 50, 5),
                new SeedProduct("Cánh gà", "Thịt", "kg", new BigDecimal("140000"), 50, 5),
                new SeedProduct("Thịt vịt", "Thịt", "kg", new BigDecimal("120000"), 40, 5),

                // ===== Hải sản =====
                new SeedProduct("Cá thu", "Hải sản", "kg", new BigDecimal("180000"), 40, 5),
                new SeedProduct("Tôm sú", "Hải sản", "kg", new BigDecimal("320000"), 30, 5),
                new SeedProduct("Tôm thẻ", "Hải sản", "kg", new BigDecimal("260000"), 30, 5),
                new SeedProduct("Cá hồi phi lê", "Hải sản", "kg", new BigDecimal("450000"), 20, 5),
                new SeedProduct("Cá basa", "Hải sản", "kg", new BigDecimal("90000"), 50, 5),
                new SeedProduct("Mực ống", "Hải sản", "kg", new BigDecimal("220000"), 25, 5),
                new SeedProduct("Mực nang", "Hải sản", "kg", new BigDecimal("200000"), 25, 5),
                new SeedProduct("Cua biển", "Hải sản", "kg", new BigDecimal("380000"), 20, 5),
                new SeedProduct("Ghẹ xanh", "Hải sản", "kg", new BigDecimal("330000"), 20, 5),
                new SeedProduct("Nghêu", "Hải sản", "kg", new BigDecimal("80000"), 40, 4),
                new SeedProduct("Sò điệp", "Hải sản", "kg", new BigDecimal("260000"), 20, 4),

                // ===== Thực phẩm chế biến sẵn =====
                new SeedProduct("Xúc xích tiệt trùng", "Thực phẩm chế biến sẵn", "gói", new BigDecimal("60000"), 80, 60),
                new SeedProduct("Chả lụa", "Thực phẩm chế biến sẵn", "đòn", new BigDecimal("90000"), 60, 30),
                new SeedProduct("Nem chua", "Thực phẩm chế biến sẵn", "hộp", new BigDecimal("55000"), 60, 30),
                new SeedProduct("Cá viên chiên", "Thực phẩm chế biến sẵn", "gói", new BigDecimal("45000"), 80, 90),
                new SeedProduct("Bò viên", "Thực phẩm chế biến sẵn", "gói", new BigDecimal("65000"), 80, 90),
                new SeedProduct("Đậu hũ", "Thực phẩm chế biến sẵn", "vỉ", new BigDecimal("15000"), 80, 10),
                new SeedProduct("Kim chi", "Thực phẩm chế biến sẵn", "hũ", new BigDecimal("50000"), 60, 90),

                // ===== Trái cây =====
                new SeedProduct("Chuối", "Trái cây", "kg", new BigDecimal("25000"), 80, 10),
                new SeedProduct("Táo", "Trái cây", "kg", new BigDecimal("75000"), 60, 21),
                new SeedProduct("Cam sành", "Trái cây", "kg", new BigDecimal("45000"), 70, 14),
                new SeedProduct("Nho xanh", "Trái cây", "kg", new BigDecimal("160000"), 40, 14),
                new SeedProduct("Dưa hấu", "Trái cây", "kg", new BigDecimal("18000"), 80, 14),
                new SeedProduct("Xoài cát", "Trái cây", "kg", new BigDecimal("65000"), 60, 10),
                new SeedProduct("Thanh long", "Trái cây", "kg", new BigDecimal("35000"), 70, 14)
        );

        for (SeedProduct sp : items) {
            Product p = upsertProduct(em, sp);
            ensureLots(em, p, sup, sp.qty, sp.shelfLifeDays, sp.sellPrice);
        }
    }

    /**
     * Add `active` column if it does not exist, with default true.
     * This is executed before any seeding so that Hibernate validate() will succeed.
     */
    private void ensureActiveColumn(EntityManager em) {
        try {
            Object res = em.createNativeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_NAME='products' AND COLUMN_NAME='active'")
                    .getSingleResult();
            long count = (res instanceof Number) ? ((Number) res).longValue() : Long.parseLong(res.toString());
            if (count == 0) {
                em.createNativeQuery(
                        "ALTER TABLE products ADD active bit NOT NULL CONSTRAINT df_products_active DEFAULT (1)")
                        .executeUpdate();
            }
        } catch (Exception e) {
            // ignore failures (e.g. permissions) - schema will be validated later
            System.err.println("ensureActiveColumn failed: " + e.getMessage());
        }
    }

    private Supplier ensureSupplier(EntityManager em) {
        return em.createQuery("SELECT s FROM Supplier s WHERE s.name = :n", Supplier.class)
                .setParameter("n", "Default Supplier")
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    Supplier sup = new Supplier("Default Supplier");
                    sup.setEmail("supplier@freshmart.local");
                    sup.setLeadTimeDays(2);
                    sup.setPhone("0900000000");
                    sup.setAddress("TP. Hồ Chí Minh");
                    em.persist(sup);
                    return sup;
                });
    }

    private Product upsertProduct(EntityManager em, SeedProduct sp) {
        Product p = em.createQuery("SELECT p FROM Product p WHERE p.name = :n", Product.class)
                .setParameter("n", sp.name)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (p == null) {
            p = new Product(sp.name, sp.sellPrice);
            em.persist(p);
        }

        p.setCategory(sp.category);
        p.setUnit(sp.unit);
        p.setSellPrice(sp.sellPrice);
        return p;
    }

    private void ensureLots(EntityManager em, Product product, Supplier supplier,
                            int qty, int shelfLifeDays, BigDecimal sellPrice) {
        Long lotCount = em.createQuery(
                        "SELECT COUNT(l) FROM ProductLot l WHERE l.product.id = :pid",
                        Long.class)
                .setParameter("pid", product.getId())
                .getSingleResult();

        if (lotCount != null && lotCount > 0) return;

        BigDecimal importPrice = sellPrice
                .multiply(new BigDecimal("0.75"))
                .setScale(2, RoundingMode.HALF_UP);

        createLot(em, product, supplier,
                Math.max(1, qty / 2),
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(Math.max(1, shelfLifeDays)),
                importPrice);

        createLot(em, product, supplier,
                qty,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(Math.max(2, shelfLifeDays * 2)),
                importPrice);
    }

    private static class SeedProduct {
        final String name;
        final String category;
        final String unit;
        final BigDecimal sellPrice;
        final int qty;
        final int shelfLifeDays;

        SeedProduct(String name, String category, String unit, BigDecimal sellPrice, int qty, int shelfLifeDays) {
            this.name = name;
            this.category = category;
            this.unit = unit;
            this.sellPrice = sellPrice;
            this.qty = qty;
            this.shelfLifeDays = shelfLifeDays;
        }
    }

    private void createLot(EntityManager em, Product product, Supplier supplier,
                           int qty, LocalDate importDate, LocalDate expiryDate, BigDecimal importPrice) {
        ProductLot lot = new ProductLot();
        lot.setProduct(product);
        lot.setSupplier(supplier);
        lot.setQtyIn(qty);
        lot.setQtyLeft(qty);
        lot.setImportDate(importDate);
        lot.setExpiryDate(expiryDate);
        lot.setImportPrice(importPrice);
        em.persist(lot);
    }
}
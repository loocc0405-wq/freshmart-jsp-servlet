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
import java.time.LocalDate;

public class BootstrapService {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepo = new UserRepository();

    /**
     * Create some default users + sample catalog for local development.
     * You can disable or remove this in production.
     */
    public void ensureDevData() {
        executor.executeVoid(this::seedIfEmpty);
    }

    private void seedIfEmpty(EntityManager em) {
        long userCount = userRepo.count(em);
        if (userCount == 0) {
            // Default accounts
            User admin = new User("admin", PasswordUtil.hash("admin123"), Role.ADMIN);
            admin.setTier(Tier.PRO);
            admin.setExpiredDate(LocalDate.now().plusYears(5));

            User staff = new User("staff", PasswordUtil.hash("staff123"), Role.STAFF);
            staff.setTier(Tier.FREE);

            User seller = new User("seller", PasswordUtil.hash("seller123"), Role.SELLER);
            seller.setTier(Tier.FREE);

            User customer = new User("customer", PasswordUtil.hash("customer123"), Role.CUSTOMER);
            customer.setTier(Tier.PRO);
            customer.setExpiredDate(LocalDate.now().plusDays(30));

            em.persist(admin);
            em.persist(staff);
            em.persist(seller);
            em.persist(customer);
        }

        Long productCount = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
        if (productCount == 0) {
            Supplier sup = new Supplier("Default Supplier");
            sup.setLeadTimeDays(2);
            em.persist(sup);

            Product p1 = new Product("Rau muống", new BigDecimal("15000"));
            p1.setCategory("Rau");
            p1.setUnit("bó");
            em.persist(p1);

            Product p2 = new Product("Thịt heo", new BigDecimal("120000"));
            p2.setCategory("Thịt");
            p2.setUnit("kg");
            em.persist(p2);

            Product p3 = new Product("Cá thu", new BigDecimal("180000"));
            p3.setCategory("Cá");
            p3.setUnit("kg");
            em.persist(p3);

            // Lots (qty_left) - expiry dates near future to demo FEFO
            createLot(em, p1, sup, 50, LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), new BigDecimal("9000"));
            createLot(em, p1, sup, 80, LocalDate.now().minusDays(2), LocalDate.now().plusDays(7), new BigDecimal("8500"));

            createLot(em, p2, sup, 30, LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), new BigDecimal("90000"));
            createLot(em, p2, sup, 60, LocalDate.now().minusDays(3), LocalDate.now().plusDays(5), new BigDecimal("88000"));

            createLot(em, p3, sup, 20, LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), new BigDecimal("140000"));
            createLot(em, p3, sup, 40, LocalDate.now().minusDays(2), LocalDate.now().plusDays(6), new BigDecimal("135000"));
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

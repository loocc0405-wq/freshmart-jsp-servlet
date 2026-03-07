package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.repository.ProductRepository;
import com.freshmart.util.JpaExecutor;

import java.util.List;

public class ProductService {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();

    public List<Product> listAll(boolean showInactive) {
        return executor.execute(em -> productRepo.findAll(em, showInactive));
    }

    // NEW: lấy danh sách category distinct
    public List<String> listCategories() {
        return executor.execute(productRepo::listCategories);
    }

    /**
     * Convenience wrapper keeping existing two‑argument signature (only active products).
     */
    public List<Product> listAll() {
        return listAll(false);
    }

    /**
     * Convenience wrapper keeping existing two‑argument search signature (only active products).
     */
    public List<Product> search(String keyword, String category) {
        return search(keyword, category, false);
    }

    public List<Product> search(String keyword, String category, boolean showInactive) {
        return executor.execute(em -> productRepo.search(em, keyword, category, showInactive));
    }
    public Product getById(Long id) {
        return executor.execute(em -> productRepo.findById(em, id).orElse(null));
    }

    public Product save(Product p) {
        return executor.execute(em -> productRepo.save(em, p));
    }

    public void deleteById(Long id) {
        executor.executeVoid(em -> productRepo.deleteById(em, id));
    }
}
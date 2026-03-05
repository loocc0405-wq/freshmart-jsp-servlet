package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.repository.ProductRepository;
import com.freshmart.util.JpaExecutor;

import java.util.List;

public class ProductService {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();

    public List<Product> listAll() {
        return executor.execute(productRepo::findAll);
    }

    // NEW: lấy danh sách category distinct
    public List<String> listCategories() {
        return executor.execute(productRepo::listCategories);
    }

    public List<Product> search(String keyword, String category) {
        return executor.execute(em -> productRepo.search(em, keyword, category));
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
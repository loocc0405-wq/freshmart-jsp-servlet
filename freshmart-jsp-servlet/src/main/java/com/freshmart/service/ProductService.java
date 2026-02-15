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

    public List<Product> search(String keyword, String category) {
        return executor.execute(em -> productRepo.search(em, keyword, category));
    }
}

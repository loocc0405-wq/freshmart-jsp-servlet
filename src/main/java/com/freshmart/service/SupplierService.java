package com.freshmart.service;

import com.freshmart.entity.Supplier;
import com.freshmart.repository.SupplierRepository;
import com.freshmart.util.JpaExecutor;
import java.util.List;

public class SupplierService {
    private final JpaExecutor executor = new JpaExecutor();
    private final SupplierRepository repo = new SupplierRepository();

    public List<Supplier> listAll() { return executor.execute(repo::findAll); }
    public Supplier getById(Long id) { return executor.execute(em -> repo.findById(em, id).orElse(null)); }
    public Supplier save(Supplier s) { return executor.execute(em -> repo.save(em, s)); }
    public void deleteById(Long id) { executor.executeVoid(em -> repo.deleteById(em, id)); }

    /**
     * Search suppliers with paging, forwarding parameters to repository.
     * page is 1-based, size must be >0.
     */
    public List<Supplier> search(String keyword, String certificate, int page, int size) {
        int offset = (page - 1) * size;
        return executor.execute(em -> repo.search(em, keyword, certificate, offset, size));
    }

    public long count(String keyword, String certificate) {
        return executor.execute(em -> repo.count(em, keyword, certificate));
    }
}

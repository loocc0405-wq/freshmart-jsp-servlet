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
}

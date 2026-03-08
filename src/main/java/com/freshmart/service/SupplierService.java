package com.freshmart.service;

import com.freshmart.entity.Supplier;
import com.freshmart.repository.SupplierRepository;
import com.freshmart.util.JpaExecutor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SupplierService {
    private final JpaExecutor executor = new JpaExecutor();
    private final SupplierRepository repo = new SupplierRepository();

    public List<Supplier> listAll() { return executor.execute(repo::findAll); }
    public Supplier getById(Long id) { return executor.execute(em -> repo.findById(em, id).orElse(null)); }
    public Supplier save(Supplier s) {
        // update audit timestamps
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (s.getId() == null) {
            s.setCreatedAt(now);
        }
        s.setUpdatedAt(now);
        return executor.execute(em -> repo.save(em, s));
    }
    public void deleteById(Long id) { executor.executeVoid(em -> repo.deleteById(em, id)); }

    /**
     * Search suppliers with paging, forwarding parameters to repository.
     * page is 1-based, size must be >0.
     */
    public List<Supplier> search(String keyword, String certificate, int page, int size) {
        int offset = (page - 1) * size;
        // delegate to date‑aware query with null bounds
        return executor.execute(em -> repo.search(em, keyword, certificate, null, null, offset, size));
    }

    public long count(String keyword, String certificate) {
        return executor.execute(em -> repo.count(em, keyword, certificate, null, null));
    }

    // new overloads that support date range filter
    public List<Supplier> search(String keyword, String certificate, LocalDate fromDate, LocalDate toDate, int page, int size) {
        // convert dates to datetimes rounding to full-day; variables must be effectively final for lambda
        final LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        final LocalDateTime end = toDate != null ? toDate.atTime(java.time.LocalTime.MAX) : null;
        int offset = (page - 1) * size;
        return executor.execute(em -> repo.search(em, keyword, certificate, start, end, offset, size));
    }

    public long count(String keyword, String certificate, LocalDate fromDate, LocalDate toDate) {
        final LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        final LocalDateTime end = toDate != null ? toDate.atTime(java.time.LocalTime.MAX) : null;
        return executor.execute(em -> repo.count(em, keyword, certificate, start, end));
    }
}

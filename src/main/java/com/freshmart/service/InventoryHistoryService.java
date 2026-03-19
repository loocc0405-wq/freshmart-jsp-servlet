package com.freshmart.service;

import com.freshmart.entity.InventoryTransaction;
import com.freshmart.entity.LotDisposal;
import com.freshmart.repository.InventoryTransactionRepository;
import com.freshmart.repository.LotDisposalRepository;
import com.freshmart.util.JpaExecutor;

import java.util.List;

public class InventoryHistoryService {

    private final JpaExecutor executor = new JpaExecutor();
    private final InventoryTransactionRepository transactionRepository = new InventoryTransactionRepository();
    private final LotDisposalRepository disposalRepository = new LotDisposalRepository();

    public List<InventoryTransaction> getRecentTransactions(int limit) {
        return executor.execute(em -> transactionRepository.findRecent(em, limit));
    }

    public List<LotDisposal> getRecentDisposals(int limit) {
        return executor.execute(em -> disposalRepository.findRecent(em, limit));
    }
}

package com.freshmart.service.dto;

import java.time.LocalDate;

public class LotConsumption {
    private final Long lotId;
    private final int qtyTaken;
    private final LocalDate expiryDate;

    public LotConsumption(Long lotId, int qtyTaken, LocalDate expiryDate) {
        this.lotId = lotId;
        this.qtyTaken = qtyTaken;
        this.expiryDate = expiryDate;
    }

    public Long getLotId() {
        return lotId;
    }

    public int getQtyTaken() {
        return qtyTaken;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}

package com.freshmart.enums;

public enum Role {
    CUSTOMER,
    SELLER,
    STAFF,
    ADMIN;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}

package com.cryptax.app.jwt;

public enum Role {
    ADMIN, USER;

    public String getAuthority() {
        return name();
    }
}

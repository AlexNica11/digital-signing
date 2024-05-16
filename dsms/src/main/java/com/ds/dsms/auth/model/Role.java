package com.ds.dsms.auth.model;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

public enum Role implements GrantedAuthority {
    ADMIN,
    USER;

    @Override
    public String toString() {
        return this.name();
    }

    @Override
    public String getAuthority() {
        return this.name();
    }
}

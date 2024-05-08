package com.ds.dsms.dss.keystore;

import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;

@Getter
public class PrivateKeyParams implements Serializable {
    @NonNull
    private final String alias;
    private final String password;

    public PrivateKeyParams(@NonNull String alias, String password) {
        this.alias = alias;
        this.password = password;
    }
}

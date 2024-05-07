package com.ds.dsms.dss.keystore;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
public class KeyStoreParams {
    @NonNull
    private final byte[] keyStore;
    @NonNull
    private final String keyStorePassword;
    @NonNull
    List<PrivateKeyParams> privateKeyParams;

    public KeyStoreParams(byte @NonNull [] keyStore, @NonNull String keyStorePassword, @NonNull List<PrivateKeyParams> privateKeyParams) {
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.privateKeyParams = privateKeyParams;
    }
}

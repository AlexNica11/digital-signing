package com.ds.dsms.dss.keystore;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Getter
public class KeyStoreParams {
    @Setter
    private byte[] keyStore;
    @NonNull
    private final String keyStoreName;
    @NonNull
    private final String keyStorePassword;
    @NonNull
    List<PrivateKeyParams> privateKeyParams;

    public KeyStoreParams(byte [] keyStore, @NonNull String keyStoreName, @NonNull String keyStorePassword, @NonNull List<PrivateKeyParams> privateKeyParams) {
        this.keyStore = keyStore;
        this.keyStoreName = keyStoreName;
        this.keyStorePassword = keyStorePassword;
        this.privateKeyParams = privateKeyParams;
    }
}

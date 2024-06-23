package com.ds.dsms.controller.dto;

import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;

@Getter
public class DocumentPayloadDTO implements Serializable {

    private final byte @NonNull [] document;

    @NonNull
    private final String documentName;

    @NonNull
    private final String signature;

    private final boolean extendSignature;

    @NonNull
    private final KeyStoreParams keyStoreParams;

    @JsonIgnore
    private final boolean encrypt;

    public DocumentPayloadDTO(byte @NonNull [] document, @NonNull String documentName, @NonNull String signature, boolean extendSignature, @NonNull KeyStoreParams keyStoreParams, boolean encrypt) {
        this.document = document;
        this.documentName = documentName;
        this.signature = signature;
        this.extendSignature = extendSignature;
        this.keyStoreParams = keyStoreParams;
        this.encrypt = encrypt;
    }
}

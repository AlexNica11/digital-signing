package com.ds.dsms.controller.dto;

import lombok.NonNull;

public class DocumentPayloadDTO {

    @NonNull
    private byte[] document;

    @NonNull
    private String documentName;

    @NonNull
    private String signature;

    private boolean encrypt;
}

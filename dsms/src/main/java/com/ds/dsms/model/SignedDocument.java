package com.ds.dsms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;

@Entity
@Table(name="SIGNED_DOCUMENT")
@Getter
public class SignedDocument {

    @Id
    @NonNull
    private String jobId;

    @Column
    private byte @NonNull [] data;

    @Column
    @NonNull
    private String documentName;

    public SignedDocument(@NonNull String jobId, byte @NonNull [] data, @NonNull String documentName) {
        this.jobId = jobId;
        this.data = data;
        this.documentName = documentName;
    }

    public SignedDocument() {

    }
}

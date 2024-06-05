package com.ds.dsms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String username;

    @Column
    @NonNull
    private String documentName;

    public SignedDocument(@NonNull String jobId, byte @NonNull [] data, String username, @NonNull String documentName) {
        this.jobId = jobId;
        this.data = data;
        this.username = username;
        this.documentName = documentName;
    }

    public SignedDocument() {

    }
}

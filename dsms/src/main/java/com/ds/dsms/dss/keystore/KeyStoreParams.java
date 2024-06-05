package com.ds.dsms.dss.keystore;

import com.ds.dsms.auth.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "KEY_STORE_PARAMS")
@Getter
@Setter
@AllArgsConstructor
public class KeyStoreParams {

    @Id
    @Column
    @NotBlank
    private String keyStoreName;

    @Column
    @NotEmpty
    private byte[] keyStoreBytes;

    @Column
    private String keyStorePassword;

    @NotEmpty
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "keyStoreParams", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PrivateKeyParams> privateKeyParams;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;


    protected KeyStoreParams() {
    }

    public KeyStoreParams(@NotBlank String keyStoreName, byte [] keyStoreBytes, String keyStorePassword, @NotEmpty Set<PrivateKeyParams> privateKeyParams) {
        this.keyStoreBytes = keyStoreBytes;
        this.keyStoreName = keyStoreName;
        this.keyStorePassword = keyStorePassword;
        this.privateKeyParams = privateKeyParams;
    }
}

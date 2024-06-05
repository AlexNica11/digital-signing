package com.ds.dsms.dss.keystore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "PRIVATE_KEY_PARAMS")
@Getter
@Setter
@AllArgsConstructor
public class PrivateKeyParams implements Serializable {
    @Id
    @Column
    @NotBlank
    private String alias;

    @Column
    private String password;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "keyStoreParams_keyStoreName")
    @JsonIgnore
    private KeyStoreParams keyStoreParams;

    protected PrivateKeyParams() {
    }

    public PrivateKeyParams(@NotBlank String alias, String password) {
        this.alias = alias;
        this.password = password;
    }
}

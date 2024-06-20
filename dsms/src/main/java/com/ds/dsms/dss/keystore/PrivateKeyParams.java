package com.ds.dsms.dss.keystore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonIgnore
    private Integer id;

    @Column
    @NotBlank
    private String alias;

    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToOne
    @JoinColumn(name = "keyStoreParams_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private KeyStoreParams keyStoreParams;

    protected PrivateKeyParams() {
    }

    public PrivateKeyParams(@NotBlank String alias, String password) {
        this.alias = alias;
        this.password = password;
    }
}

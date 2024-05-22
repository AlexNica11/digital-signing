package com.ds.dsms.model;

import com.ds.dsms.auth.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Table(name = "KEY_STORE_STORAGE")
@Getter
@AllArgsConstructor
public class KeyStoreStorage {

    @Id
    @Column
    @NotBlank
    private String fileName;

    @Column
    @NotEmpty
    private byte[] data;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    protected KeyStoreStorage() {
    }
}

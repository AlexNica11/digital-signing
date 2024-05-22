package com.ds.dsms.auth.model;

import com.ds.dsms.model.KeyStoreStorage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name="security_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank
    @Column(name = "username")
    private String username;

    @NotBlank
    @Column(name = "password")
    @JsonIgnore
    private String password;

    @NotBlank
    @Email
    @Column(name = "email")
    private String email;

    @NotEmpty
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "roles")
    private List<Role> roles;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    @Setter
    private List<KeyStoreStorage> keyStores;

    protected User() {
    }

    public User(String username, String password, String email, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.keyStores = new ArrayList<>();
    }

    public User(Integer id, String username, String password, String email, List<Role> roles, List<KeyStoreStorage> keyStores) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.keyStores = keyStores;
    }
}

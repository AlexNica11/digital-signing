package com.ds.dsms.auth.model;

import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name="security_user")
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
    private Set<Role> roles;

    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<KeyStoreParams> keyStoreParams = new HashSet<>();

    protected User() {
    }

    public User(String username, String password, String email, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.keyStoreParams = new HashSet<>();
    }
}

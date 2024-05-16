package com.ds.dsms.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    private String email;

    public LoginDTO() {
    }

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginDTO(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}

package com.ds.dsms.controller;

import com.ds.dsms.auth.UserService;
import com.ds.dsms.auth.dto.LoginDTO;
import com.ds.dsms.auth.model.User;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    public String login(@RequestBody @Valid LoginDTO loginDto) {
        String jwt = userService.login(loginDto.getUsername(), loginDto.getPassword()).orElseThrow(()->
                new HttpServerErrorException(HttpStatus.FORBIDDEN, "Login Failed"));
        return "{\"jwt\":\"" + jwt + "\"}";
    }

    @PostMapping("/signup")
//    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public User signup(@RequestBody @Valid LoginDTO loginDto) {
        return userService.signup(loginDto.getUsername(), loginDto.getPassword(), loginDto.getEmail())
                .orElseThrow(() -> new HttpServerErrorException(HttpStatus.BAD_REQUEST,"User already exists"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAll();
    }

    @PostMapping("/uploadKeyStore")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadKeyStore(@RequestBody @Valid KeyStoreParams keyStoreParams, @RequestHeader("Authorization") String jwtToken) {
        userService.uploadKeyStore(keyStoreParams, jwtToken);
    }

    @PostMapping("/keyStores")
    public List<String> getKeyStores(@RequestHeader("Authorization") String jwtToken){
        return userService.getKeyStores(jwtToken);
    }

    @PostMapping("/privateKeyParams")
    public List<String> getPrivateKeyParams(@RequestBody String keyStoreName, @RequestHeader("Authorization") String jwtToken){
        return userService.getPrivateKeyParams(keyStoreName, jwtToken);
    }

    @PostMapping("/uploadKeyStoreForm")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadKeyStoreWithForm(@RequestParam MultipartFile file, @RequestParam String keyStoreName, @RequestParam String keyStorePassword, @RequestParam String privateKeyParams, @RequestHeader("Authorization") String jwtToken) throws IOException {
        PrivateKeyParams[] privateKeyParamsObject = new ObjectMapper().readValue(privateKeyParams, PrivateKeyParams[].class);

        KeyStoreParams keyStoreParams = new KeyStoreParams(keyStoreName, file.getBytes(), keyStorePassword, Set.of(privateKeyParamsObject));
        userService.uploadKeyStore(keyStoreParams, jwtToken);
    }
}

package com.ds.dsms.auth;

import com.ds.dsms.auth.jwt.JWTProvider;
import com.ds.dsms.auth.jwt.JWTTokenFilter;
import com.ds.dsms.auth.model.Role;
import com.ds.dsms.auth.model.User;
import com.ds.dsms.auth.repo.UserRepository;
import com.ds.dsms.exception.UserException;
import com.ds.dsms.model.KeyStoreStorage;
import com.ds.dsms.repo.KeyStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JWTProvider jwtProvider;
    private final KeyStoreRepository keyStoreRepository;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JWTProvider jwtProvider, KeyStoreRepository keyStoreRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.keyStoreRepository = keyStoreRepository;
    }

    public Optional<String> login(String username, String password) {
        LOGGER.info("New user attempting to login");
        Optional<String> token = Optional.empty();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
                token = Optional.of(jwtProvider.createToken(username, user.get().getRoles()));
            } catch (AuthenticationException e){
                LOGGER.info("Log in failed for user {}", username);
            }
        }

        return token;
    }

    public Optional<User> signup(String username, String password, String email) {
        LOGGER.info("New user attempting to signup");
        Optional<User> user = Optional.empty();
        if (userRepository.findByUsername(username).isEmpty()) {
            user = Optional.of(userRepository.save(new User(
                    username,
                    passwordEncoder.encode(password),
                    email,
                    Arrays.asList(Role.USER)
            )));
        }
        return user;
    }
    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void uploadKeyStore(MultipartFile file, String jwtToken) throws IOException, KeyStoreException {
        String username = jwtProvider.getUsername(jwtToken.replace(JWTTokenFilter.BEARER, "").trim());
        String id = file.getResource().getFilename();
        if(keyStoreRepository.existsById(id)){
            throw new KeyStoreException("Key store already exists");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User: " + username + " not found"));
        KeyStoreStorage keyStore= keyStoreRepository.save(new KeyStoreStorage(id, file.getBytes(), user));
        if(user.getKeyStores().isEmpty()){
            user.setKeyStores(List.of(keyStore));
        } else {
            List<KeyStoreStorage> keyStores = user.getKeyStores();
            keyStores.add(keyStore);
            user.setKeyStores(keyStores);
        }
        userRepository.save(user);
    }
}

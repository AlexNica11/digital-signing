package com.ds.dsms.auth;

import com.ds.dsms.auth.jwt.JWTProvider;
import com.ds.dsms.auth.jwt.JWTTokenFilter;
import com.ds.dsms.auth.model.Role;
import com.ds.dsms.auth.model.User;
import com.ds.dsms.auth.repo.UserRepository;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.exception.UserException;
import com.ds.dsms.repo.KeyStoreRepository;
import com.ds.dsms.repo.PrivateKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JWTProvider jwtProvider;
    private final KeyStoreRepository keyStoreRepository;
    private final PrivateKeyRepository privateKeyRepository;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JWTProvider jwtProvider, KeyStoreRepository keyStoreRepository, PrivateKeyRepository privateKeyRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.keyStoreRepository = keyStoreRepository;
        this.privateKeyRepository = privateKeyRepository;
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
                    Set.of(Role.USER)
            )));
        }
        return user;
    }
    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void uploadKeyStore(KeyStoreParams keyStoreParams, String jwtToken) throws IOException, KeyStoreException {
        String username = jwtProvider.getUsername(jwtToken.replace(JWTTokenFilter.BEARER, "").trim());
        if(keyStoreRepository.existsById(keyStoreParams.getKeyStoreName())){
            throw new KeyStoreException("Key store already exists");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User: " + username + " not found"));

        keyStoreParams.setUser(user);
        KeyStoreParams keyStoreParamsSaved = keyStoreRepository.save(keyStoreParams);

        if(user.getKeyStoreParams().isEmpty()){
            user.setKeyStoreParams(Set.of(keyStoreParamsSaved));
        } else {
            Set<KeyStoreParams> keyStores = user.getKeyStoreParams();
            keyStores.add(keyStoreParamsSaved);
            user.setKeyStoreParams(keyStores);
        }
        
        userRepository.save(user);
    }
}

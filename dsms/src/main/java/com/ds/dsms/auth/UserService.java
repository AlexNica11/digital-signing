package com.ds.dsms.auth;

import com.ds.dsms.auth.jwt.JWTProvider;
import com.ds.dsms.auth.model.Role;
import com.ds.dsms.auth.model.User;
import com.ds.dsms.auth.repo.UserRepository;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import com.ds.dsms.exception.KeyStoreException;
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

import java.util.*;

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
                    Set.of(Role.ROLE_USER)
            )));
        }
        return user;
    }
    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void uploadKeyStore(KeyStoreParams keyStoreParams, String jwtToken) {
        LOGGER.info("New keystore attempting to upload");

        User user = getUserFromToken(jwtToken);

        if(keyStoreRepository.existsByKeyStoreNameAndUser(keyStoreParams.getKeyStoreName(), user)){
            throw new KeyStoreException("Key store already exists");
        }

        for(PrivateKeyParams privateKeyParams : keyStoreParams.getPrivateKeyParams()){
            privateKeyParams.setKeyStoreParams(keyStoreParams);
        }

        keyStoreParams.setUser(user);
        KeyStoreParams keyStoreParamsSaved = keyStoreRepository.save(keyStoreParams);
    }

    public List<String> getKeyStores(String jwtToken){
        List<KeyStoreParams> keyStores = keyStoreRepository.findByUser(getUserFromToken(jwtToken));
        List<String> nameList = new ArrayList<>();

        for(KeyStoreParams keyStore : keyStores){
            nameList.add(keyStore.getKeyStoreName());
        }

        return nameList;
    }

    public List<String> getPrivateKeyParams(String keyStoreName, String jwtToken){
        KeyStoreParams keyStoreParams = keyStoreRepository.findByKeyStoreNameAndUser(keyStoreName, getUserFromToken(jwtToken)).orElseThrow(() -> new KeyStoreException("Key store not found"));
        List<String> nameList = new ArrayList<>();

        for(PrivateKeyParams privateKeyParam : keyStoreParams.getPrivateKeyParams()){
            nameList.add(privateKeyParam.getAlias());
        }

        return nameList;
    }

    private User getUserFromToken(String jwtToken) {
        String username = jwtProvider.getUsernameFromBearerToken(jwtToken);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User: " + username + " not found"));
        return user;
    }
}

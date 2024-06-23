package com.ds.dsms.auth;

import com.ds.dsms.auth.jwt.JWTProvider;
import com.ds.dsms.auth.model.User;
import com.ds.dsms.auth.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.security.core.userdetails.User.withUsername;

@Component
public class DSMSUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JWTProvider jwtProvider;

    public DSMSUserDetailsService(UserRepository userRepository, JWTProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Username: " + username + " not found"));
        return withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public Optional<UserDetails> loadUserByJWTToken(String jwtToken) {
        if (jwtProvider.isTokenValid(jwtToken)) {
            return Optional.of(
                    withUsername(jwtProvider.getUsername(jwtToken))
                    .authorities(jwtProvider.getRoles(jwtToken))
                    .password("") //token does not have password but field may not be empty
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build());
        }
        return Optional.empty();
    }
}

package com.ds.dsms.auth.jwt;

import com.ds.dsms.auth.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jose4j.keys.HmacKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JWTProvider {

    private final String ROLES_KEY = "roles";
    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JWTProvider(@Value("${security.jwt.token.secret-key}") String secretKey, @Value("${security.jwt.token.expiration}") long validityInMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getEncoder().encodeToString(secretKey.getBytes()).getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(String username, List<Role> roles) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .subject(username)
                .claim(ROLES_KEY, roles.stream().map(role -> new SimpleGrantedAuthority(role.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()))
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public List<GrantedAuthority> getRoles(String token) {
        List<Map<String, String>> roleClaims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(ROLES_KEY, List.class);
        return roleClaims.stream().map(roleClaim -> new SimpleGrantedAuthority(roleClaim.get("authority"))).collect(Collectors.toList());
    }
}

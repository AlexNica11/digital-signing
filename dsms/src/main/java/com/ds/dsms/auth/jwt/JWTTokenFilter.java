package com.ds.dsms.auth.jwt;

import com.ds.dsms.auth.DSMSUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Optional;

public class JWTTokenFilter extends GenericFilterBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTTokenFilter.class);
    private static final String BEARER = "Bearer";
    private static final String AUTHORIZATION = "Authorization";

    private final DSMSUserDetailsService userDetailsService;

    public JWTTokenFilter(DSMSUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOGGER.info("Process request to check for a JSON Web Token ");
        //Check for Authorization:Bearer JWT
        String authHeaderValue = ((HttpServletRequest) request).getHeader(AUTHORIZATION);
        //Pull the Username and Roles from the JWT to construct the user details
        getBearerToken(authHeaderValue).flatMap(userDetailsService::loadUserByJWTToken).ifPresent(userDetails -> {
            //Add the user details (Permissions) to the Context for just this API invocation
            SecurityContextHolder.getContext().setAuthentication(
                    new PreAuthenticatedAuthenticationToken(userDetails, "", userDetails.getAuthorities()));
        });

        //move on to the next filter in the chains
        chain.doFilter(request, response);
    }

    private Optional<String> getBearerToken(String authHeaderValue) {
        if (authHeaderValue != null && authHeaderValue.startsWith(BEARER)) {
            return Optional.of(authHeaderValue.replace(BEARER, "").trim());
        }
        return Optional.empty();
    }
}

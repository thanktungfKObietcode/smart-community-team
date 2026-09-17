package vn.edu.crs.smartcommunity.identity.internal.service;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import vn.edu.crs.smartcommunity.identity.internal.config.JwtProperties;
import vn.edu.crs.smartcommunity.identity.internal.dto.LoginRequest;
import vn.edu.crs.smartcommunity.identity.internal.dto.LoginResponse;
import vn.edu.crs.smartcommunity.identity.internal.security.ApplicationUserDetails;

@Service
public class IdentityAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final long tokenLifetimeSeconds;

    public IdentityAuthenticationService(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.tokenLifetimeSeconds = jwtProperties.expirationSeconds();
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim();
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        } catch (DisabledException exception) {
            throw exception;
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        ApplicationUserDetails user = (ApplicationUserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenService.createToken(user, Instant.now());
        return new LoginResponse(
                accessToken,
                "Bearer",
                tokenLifetimeSeconds,
                user.getUserId(),
                user.getFullName(),
                user.getUsername(),
                user.getRoles());
    }

    public static final class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid email or password");
        }
    }
}

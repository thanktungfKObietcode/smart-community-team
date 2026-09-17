package vn.edu.crs.smartcommunity.identity.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.identity.internal.dto.CurrentUserResponse;
import vn.edu.crs.smartcommunity.identity.internal.dto.LoginRequest;
import vn.edu.crs.smartcommunity.identity.internal.dto.LoginResponse;
import vn.edu.crs.smartcommunity.identity.internal.service.IdentityAuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class IdentityController {

    private final IdentityAuthenticationService authenticationService;

    public IdentityController(IdentityAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new CurrentUserResponse(
                userId == null ? null : userId.longValue(),
                jwt.getClaimAsString("fullName"),
                jwt.getSubject(),
                roles == null ? List.of() : roles);
    }
}

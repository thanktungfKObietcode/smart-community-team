package vn.edu.crs.smartcommunity.identity.internal.dto;

import java.util.Set;

public record ManagedUserResponse(
        Long userId,
        String fullName,
        String email,
        Set<String> roles,
        boolean active
) {
    public ManagedUserResponse {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}

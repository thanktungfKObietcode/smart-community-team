package vn.edu.crs.smartcommunity.identity.api;

import java.util.Set;

public record IdentityUserInfo(
        Long userId,
        String fullName,
        String email,
        Set<String> roles,
        boolean active
) {
    public IdentityUserInfo {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}

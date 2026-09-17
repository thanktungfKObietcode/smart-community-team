package vn.edu.crs.smartcommunity.identity.internal.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String fullName,
        String email,
        List<String> roles
) {
}

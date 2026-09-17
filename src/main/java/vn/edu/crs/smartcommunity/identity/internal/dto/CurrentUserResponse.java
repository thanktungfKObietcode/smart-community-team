package vn.edu.crs.smartcommunity.identity.internal.dto;

import java.util.List;

public record CurrentUserResponse(
        Long userId,
        String fullName,
        String email,
        List<String> roles
) {
}

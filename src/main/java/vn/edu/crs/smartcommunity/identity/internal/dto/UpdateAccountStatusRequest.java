package vn.edu.crs.smartcommunity.identity.internal.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {
}

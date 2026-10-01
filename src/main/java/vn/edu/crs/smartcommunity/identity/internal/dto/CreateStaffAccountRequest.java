package vn.edu.crs.smartcommunity.identity.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import vn.edu.crs.smartcommunity.identity.api.StaffAccountRole;

public record CreateStaffAccountRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @NotBlank(message = "Initial password is required")
        @Size(min = 6, max = 100, message = "Initial password must contain at least 6 characters")
        String initialPassword,

        @NotNull(message = "Staff role is required")
        StaffAccountRole role
) {
}

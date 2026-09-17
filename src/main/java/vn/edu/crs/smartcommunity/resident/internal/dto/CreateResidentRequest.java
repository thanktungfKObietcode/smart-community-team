package vn.edu.crs.smartcommunity.resident.internal.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import vn.edu.crs.smartcommunity.resident.internal.entity.ResidentType;

public record CreateResidentRequest(
        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,

        @NotNull(message = "Apartment ID is required")
        @Positive(message = "Apartment ID must be positive")
        Long apartmentId,

        @NotNull(message = "Resident type is required")
        ResidentType residentType,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "Phone contains invalid characters")
        String phone,

        @PastOrPresent(message = "Move-in date cannot be in the future")
        LocalDate moveInDate
) {
}

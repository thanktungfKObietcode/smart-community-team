package vn.edu.crs.smartcommunity.resident.internal.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import vn.edu.crs.smartcommunity.resident.internal.entity.ResidentType;

public record CreateResidentRequest(
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Size(min = 6, max = 100, message = "Initial password must contain at least 6 characters")
        String initialPassword,

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

    /**
     * Compatibility constructor for existing integrations that link a pre-existing
     * RESIDENT account. New management workflows should omit userId and provide
     * fullName, email and initialPassword instead.
     */
    public CreateResidentRequest(
            Long userId,
            Long apartmentId,
            ResidentType residentType,
            String phone,
            LocalDate moveInDate) {
        this(null, null, null, userId, apartmentId, residentType, phone, moveInDate);
    }
}

package vn.edu.crs.smartcommunity.property.internal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateApartmentRequest(
        @NotBlank(message = "Unit number is required")
        @Size(max = 30, message = "Unit number must not exceed 30 characters")
        String unitNumber,

        @NotNull(message = "Floor number is required")
        @Min(value = 1, message = "Floor number must be at least 1")
        Integer floorNumber
) {
}

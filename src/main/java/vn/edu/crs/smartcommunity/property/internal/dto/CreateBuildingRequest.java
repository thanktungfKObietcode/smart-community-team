package vn.edu.crs.smartcommunity.property.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBuildingRequest(
        @NotBlank(message = "Building code is required")
        @Size(max = 30, message = "Building code must not exceed 30 characters")
        String code,

        @NotBlank(message = "Building name is required")
        @Size(max = 150, message = "Building name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Building address is required")
        @Size(max = 255, message = "Building address must not exceed 255 characters")
        String address
) {
}

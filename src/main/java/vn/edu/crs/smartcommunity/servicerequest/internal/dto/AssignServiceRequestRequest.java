package vn.edu.crs.smartcommunity.servicerequest.internal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignServiceRequestRequest(
        @NotNull(message = "Technician ID is required")
        @Positive(message = "Technician ID must be positive")
        Long technicianId
) {
}

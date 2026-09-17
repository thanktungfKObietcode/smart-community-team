package vn.edu.crs.smartcommunity.servicerequest.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestPriority;

public record CreateServiceRequestRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @Size(max = 80, message = "Category must not exceed 80 characters")
        String category,

        @NotNull(message = "Priority is required")
        ServiceRequestPriority priority
) {
}

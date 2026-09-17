package vn.edu.crs.smartcommunity.servicerequest.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveServiceRequestRequest(
        @NotBlank(message = "Resolution note is required")
        @Size(max = 2000, message = "Resolution note must not exceed 2000 characters")
        String resolutionNote
) {
}

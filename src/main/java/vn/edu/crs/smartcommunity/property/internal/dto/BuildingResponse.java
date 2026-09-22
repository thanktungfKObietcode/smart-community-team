package vn.edu.crs.smartcommunity.property.internal.dto;

import java.time.Instant;

public record BuildingResponse(
        Long id,
        String code,
        String name,
        String address,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}

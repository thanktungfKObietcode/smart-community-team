package vn.edu.crs.smartcommunity.property.internal.dto;

import java.time.Instant;

public record ApartmentResponse(
        Long id,
        String unitNumber,
        Integer floorNumber,
        boolean active,
        Long buildingId,
        String buildingCode,
        String buildingName,
        Instant createdAt,
        Instant updatedAt
) {
}

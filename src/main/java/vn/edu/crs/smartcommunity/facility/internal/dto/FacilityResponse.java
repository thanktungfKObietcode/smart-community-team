package vn.edu.crs.smartcommunity.facility.internal.dto;

import java.time.Instant;
import java.time.LocalTime;

import vn.edu.crs.smartcommunity.facility.api.FacilityStatus;
import vn.edu.crs.smartcommunity.facility.api.FacilityType;

public record FacilityResponse(
        Long id,
        String code,
        String name,
        String description,
        Long buildingId,
        FacilityType type,
        FacilityStatus status,
        boolean bookable,
        LocalTime openingTime,
        LocalTime closingTime,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}

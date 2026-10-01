package vn.edu.crs.smartcommunity.facility.internal.dto;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import vn.edu.crs.smartcommunity.facility.api.FacilityStatus;
import vn.edu.crs.smartcommunity.facility.api.FacilityType;

public record FacilityResponse(
        Long id,
        String code,
        String name,
        String description,
        Long buildingId,
        String location,
        Integer capacity,
        String coverImageUrl,
        List<String> galleryImageUrls,
        FacilityType type,
        FacilityStatus status,
        boolean bookable,
        LocalTime openingTime,
        LocalTime closingTime,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}

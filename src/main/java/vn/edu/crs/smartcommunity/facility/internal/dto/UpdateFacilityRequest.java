package vn.edu.crs.smartcommunity.facility.internal.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.Size;

import vn.edu.crs.smartcommunity.facility.api.FacilityStatus;
import vn.edu.crs.smartcommunity.facility.api.FacilityType;

public record UpdateFacilityRequest(
        @Size(max = 150) String name,
        @Size(max = 1000) String description,
        Long buildingId,
        FacilityType type,
        FacilityStatus status,
        Boolean bookable,
        LocalTime openingTime,
        LocalTime closingTime,
        Boolean active) {
}

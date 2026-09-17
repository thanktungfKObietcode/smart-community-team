package vn.edu.crs.smartcommunity.facility.api;

import java.time.LocalTime;

public record FacilityInfo(
        Long id,
        String code,
        String name,
        Long buildingId,
        FacilityStatus status,
        boolean bookable,
        LocalTime openingTime,
        LocalTime closingTime,
        boolean active) {
}

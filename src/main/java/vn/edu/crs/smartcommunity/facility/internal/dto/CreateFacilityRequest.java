package vn.edu.crs.smartcommunity.facility.internal.dto;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import vn.edu.crs.smartcommunity.facility.api.FacilityType;

public record CreateFacilityRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        Long buildingId,
        @Size(max = 180) String location,
        Integer capacity,
        @Size(max = 500) String coverImageUrl,
        List<@Size(max = 500) String> galleryImageUrls,
        @NotNull FacilityType type,
        Boolean bookable,
        @NotNull LocalTime openingTime,
        @NotNull LocalTime closingTime) {
}

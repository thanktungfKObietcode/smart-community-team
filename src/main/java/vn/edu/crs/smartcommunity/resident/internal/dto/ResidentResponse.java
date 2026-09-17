package vn.edu.crs.smartcommunity.resident.internal.dto;

import java.time.LocalDate;

import vn.edu.crs.smartcommunity.property.api.ApartmentInfo;
import vn.edu.crs.smartcommunity.resident.internal.entity.ResidentType;

public record ResidentResponse(
        Long residentId,
        Long userId,
        String fullName,
        String email,
        String phone,
        ResidentType residentType,
        boolean active,
        LocalDate moveInDate,
        ApartmentInfo apartment
) {
}

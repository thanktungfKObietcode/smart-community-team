package vn.edu.crs.smartcommunity.resident.api;

public record ResidentInfo(
        Long residentId,
        Long userId,
        Long apartmentId,
        boolean active
) {
}

package vn.edu.crs.smartcommunity.facility.api;

public record FacilityUpdatedEvent(Long facilityId, String code, String name, Long actorUserId) {
}

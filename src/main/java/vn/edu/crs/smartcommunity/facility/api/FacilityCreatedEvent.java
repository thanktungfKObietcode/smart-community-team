package vn.edu.crs.smartcommunity.facility.api;

public record FacilityCreatedEvent(Long facilityId, String code, String name, Long actorUserId) {
}

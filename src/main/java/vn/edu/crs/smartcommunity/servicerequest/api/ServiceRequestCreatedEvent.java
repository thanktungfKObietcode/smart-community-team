package vn.edu.crs.smartcommunity.servicerequest.api;

public record ServiceRequestCreatedEvent(
        Long requestId,
        Long residentUserId,
        String title,
        Long actorUserId
) {
}

package vn.edu.crs.smartcommunity.servicerequest.api;

public record ServiceRequestClosedEvent(
        Long requestId,
        Long residentUserId,
        String title,
        Long actorUserId
) {
}

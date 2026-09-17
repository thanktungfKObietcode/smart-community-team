package vn.edu.crs.smartcommunity.servicerequest.api;

public record ServiceRequestStartedEvent(
        Long requestId,
        Long technicianUserId,
        String title,
        Long actorUserId) {
}

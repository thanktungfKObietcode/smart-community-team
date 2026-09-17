package vn.edu.crs.smartcommunity.servicerequest.api;

public record ServiceRequestAssignedEvent(
        Long requestId,
        Long technicianUserId,
        String title,
        Long actorUserId
) {
}

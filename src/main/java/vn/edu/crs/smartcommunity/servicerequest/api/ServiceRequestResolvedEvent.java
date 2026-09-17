package vn.edu.crs.smartcommunity.servicerequest.api;

public record ServiceRequestResolvedEvent(
        Long requestId,
        Long residentUserId,
        String title,
        Long actorUserId
) {
}

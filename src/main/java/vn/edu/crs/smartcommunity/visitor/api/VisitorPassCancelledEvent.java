package vn.edu.crs.smartcommunity.visitor.api;

public record VisitorPassCancelledEvent(Long passId, Long residentUserId, String code, Long actorUserId) {
}

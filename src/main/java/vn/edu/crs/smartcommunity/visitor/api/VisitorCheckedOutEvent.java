package vn.edu.crs.smartcommunity.visitor.api;

public record VisitorCheckedOutEvent(Long passId, Long residentUserId, String code, Long actorUserId) {
}

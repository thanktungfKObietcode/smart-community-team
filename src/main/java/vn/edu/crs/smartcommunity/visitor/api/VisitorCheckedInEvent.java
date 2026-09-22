package vn.edu.crs.smartcommunity.visitor.api;

public record VisitorCheckedInEvent(Long passId, Long residentUserId, String code, String visitorName, Long actorUserId) {
}

package vn.edu.crs.smartcommunity.visitor.api;

import java.time.LocalDateTime;

public record VisitorPassCreatedEvent(Long passId, Long residentUserId, String code,
        String visitorName, LocalDateTime validFrom, LocalDateTime validUntil) {
}

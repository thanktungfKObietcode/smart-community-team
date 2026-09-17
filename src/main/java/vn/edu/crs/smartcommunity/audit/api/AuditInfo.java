package vn.edu.crs.smartcommunity.audit.api;

import java.time.Instant;

public record AuditInfo(
        String action, String entityType, Long entityId, String description,
        Instant timestamp, Long actorUserId, String actorFullName) {
}

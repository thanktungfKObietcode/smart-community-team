package vn.edu.crs.smartcommunity.servicerequest.api;

import java.time.Instant;

public record ServiceRequestSummary(
        Long id, String code, String title, String category, String priority, String status,
        Instant dueAt, boolean overdue, Instant createdAt) {
}

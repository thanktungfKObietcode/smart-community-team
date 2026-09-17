package vn.edu.crs.smartcommunity.notification.internal.dto;

import java.time.Instant;

import vn.edu.crs.smartcommunity.notification.internal.entity.NotificationType;

public record NotificationResponse(
        Long id,
        Long userId,
        NotificationType type,
        String title,
        String message,
        boolean read,
        Instant createdAt
) {
}

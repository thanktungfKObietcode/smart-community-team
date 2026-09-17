package vn.edu.crs.smartcommunity.notification.internal.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.notification.internal.dto.NotificationResponse;
import vn.edu.crs.smartcommunity.notification.internal.dto.ReadAllNotificationsResponse;
import vn.edu.crs.smartcommunity.notification.internal.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    public List<NotificationResponse> myNotifications(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.listForUser(userId(jwt));
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Jwt jwt) {
        return notificationService.markRead(notificationId, userId(jwt));
    }

    @PatchMapping("/read-all")
    public ReadAllNotificationsResponse markAllRead(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.markAllRead(userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new IllegalStateException("Authenticated token has no userId claim");
        }
        return userId.longValue();
    }
}

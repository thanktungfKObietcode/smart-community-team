package vn.edu.crs.smartcommunity.notification.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.notification.internal.dto.NotificationResponse;
import vn.edu.crs.smartcommunity.notification.internal.dto.ReadAllNotificationsResponse;
import vn.edu.crs.smartcommunity.notification.internal.entity.Notification;
import vn.edu.crs.smartcommunity.notification.internal.entity.NotificationType;
import vn.edu.crs.smartcommunity.notification.internal.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public ReadAllNotificationsResponse markAllRead(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
        int updatedCount = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                updatedCount++;
            }
        }
        notificationRepository.saveAll(notifications);
        return new ReadAllNotificationsResponse(updatedCount);
    }

    @Transactional
    public NotificationResponse create(
            Long userId,
            NotificationType type,
            String title,
            String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}

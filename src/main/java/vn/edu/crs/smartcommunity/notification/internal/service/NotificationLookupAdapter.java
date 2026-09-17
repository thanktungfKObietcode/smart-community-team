package vn.edu.crs.smartcommunity.notification.internal.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.notification.api.NotificationLookup;
import vn.edu.crs.smartcommunity.notification.internal.repository.NotificationRepository;

@Component
public class NotificationLookupAdapter implements NotificationLookup {

    private final NotificationRepository repository;

    public NotificationLookupAdapter(NotificationRepository repository) { this.repository = repository; }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadByUserId(Long userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }
}

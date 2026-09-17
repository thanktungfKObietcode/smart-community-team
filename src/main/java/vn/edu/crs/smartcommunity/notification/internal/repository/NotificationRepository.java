package vn.edu.crs.smartcommunity.notification.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.notification.internal.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByUserIdAndReadFalse(Long userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}

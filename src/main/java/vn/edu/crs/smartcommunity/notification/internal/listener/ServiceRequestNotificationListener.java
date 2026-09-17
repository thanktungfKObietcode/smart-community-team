package vn.edu.crs.smartcommunity.notification.internal.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import vn.edu.crs.smartcommunity.notification.internal.entity.NotificationType;
import vn.edu.crs.smartcommunity.notification.internal.service.NotificationService;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestAssignedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestResolvedEvent;

@Component
public class ServiceRequestNotificationListener {

    private final NotificationService notificationService;

    public ServiceRequestNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onAssigned(ServiceRequestAssignedEvent event) {
        notificationService.create(
                event.technicianUserId(),
                NotificationType.SERVICE_REQUEST_ASSIGNED,
                "Service request assigned",
                "Service request #" + event.requestId() + " has been assigned to you: " + event.title());
    }

    @EventListener
    public void onResolved(ServiceRequestResolvedEvent event) {
        notificationService.create(
                event.residentUserId(),
                NotificationType.SERVICE_REQUEST_RESOLVED,
                "Service request resolved",
                "Your service request #" + event.requestId() + " has been resolved: " + event.title());
    }
}

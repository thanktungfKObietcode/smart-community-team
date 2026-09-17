package vn.edu.crs.smartcommunity.notification.internal.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import vn.edu.crs.smartcommunity.booking.api.BookingConfirmedEvent;
import vn.edu.crs.smartcommunity.notification.internal.entity.NotificationType;
import vn.edu.crs.smartcommunity.notification.internal.service.NotificationService;
import vn.edu.crs.smartcommunity.visitor.api.VisitorCheckedInEvent;

@Component
public class BookingVisitorNotificationListener {

    private final NotificationService notificationService;

    public BookingVisitorNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        notificationService.create(event.residentUserId(), NotificationType.BOOKING_CONFIRMED,
                "Đặt tiện ích thành công",
                "Bạn đã đặt " + event.facilityName() + " từ " + event.startTime() + " đến " + event.endTime() + ".");
    }

    @EventListener
    public void onVisitorCheckedIn(VisitorCheckedInEvent event) {
        notificationService.create(event.residentUserId(), NotificationType.VISITOR_CHECKED_IN,
                "Khách đã check-in", "Khách " + event.visitorName() + " của bạn đã check-in.");
    }
}

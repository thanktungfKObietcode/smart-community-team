package vn.edu.crs.smartcommunity.booking.api;

import java.time.LocalDateTime;

public record BookingConfirmedEvent(
        Long bookingId,
        Long residentUserId,
        Long facilityId,
        String facilityName,
        LocalDateTime startTime,
        LocalDateTime endTime) {
}

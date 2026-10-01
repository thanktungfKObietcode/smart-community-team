package vn.edu.crs.smartcommunity.booking.internal.dto;

import java.time.LocalDateTime;

/** A confirmed time interval, intentionally without any resident information. */
public record BookingTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
}

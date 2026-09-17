package vn.edu.crs.smartcommunity.booking.internal.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import vn.edu.crs.smartcommunity.booking.internal.entity.BookingStatus;

public record BookingResponse(
        Long id,
        String code,
        Long residentId,
        Long facilityId,
        String facilityName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant cancelledAt) {
}

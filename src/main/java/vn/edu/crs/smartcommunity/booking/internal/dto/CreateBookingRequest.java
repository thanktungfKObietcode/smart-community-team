package vn.edu.crs.smartcommunity.booking.internal.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Long facilityId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime) {
}

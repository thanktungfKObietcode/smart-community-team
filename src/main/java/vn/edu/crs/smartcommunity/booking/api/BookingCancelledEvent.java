package vn.edu.crs.smartcommunity.booking.api;

public record BookingCancelledEvent(Long bookingId, Long residentUserId, Long facilityId) {
}

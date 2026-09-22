package vn.edu.crs.smartcommunity.reporting.internal.dto;

public record ResidentDashboardResponse(
        ResidentSummary resident,
        ServiceRequestCounts serviceRequests,
        BookingCounts bookings,
        VisitorPassCounts visitorPasses,
        long unreadNotifications) {

    public record ResidentSummary(String fullName, String apartment, String building) { }

    public record ServiceRequestCounts(long open, long inProgress, long resolved) { }

    public record BookingCounts(long upcoming) { }

    public record VisitorPassCounts(long active) { }
}

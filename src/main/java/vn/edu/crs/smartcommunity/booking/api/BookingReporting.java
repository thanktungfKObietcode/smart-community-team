package vn.edu.crs.smartcommunity.booking.api;

public interface BookingReporting {

    BookingMetrics getMetrics();

    long countUpcomingByResidentId(Long residentId);
}

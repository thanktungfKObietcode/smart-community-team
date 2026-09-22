package vn.edu.crs.smartcommunity.reporting.internal.dto;

import java.util.List;
import java.util.Map;

import vn.edu.crs.smartcommunity.audit.api.AuditInfo;

public record ManagerDashboardResponse(
        ServiceRequestDashboard serviceRequests,
        ResidentDashboard residents,
        FacilityDashboard facilities,
        BookingDashboard bookings,
        VisitorDashboard visitors,
        List<AuditInfo> recentActivity) {

    public record ServiceRequestDashboard(long total, long open, long assigned, long inProgress,
            long resolved, long closed, long cancelled, long overdue,
            Map<String, Long> requestsByCategory, long averageResolutionMinutes) { }

    public record ResidentDashboard(long active) { }

    public record FacilityDashboard(long total, long available, long maintenance) { }

    public record BookingDashboard(long today, long confirmed) { }

    public record VisitorDashboard(long activeToday, long checkedIn) { }
}

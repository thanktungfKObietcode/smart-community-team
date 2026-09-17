package vn.edu.crs.smartcommunity.servicerequest.api;

import java.util.List;

public record TechnicianServiceRequestMetrics(
        long assigned, long inProgress, long resolved, long overdue,
        List<ServiceRequestSummary> recentTasks) {
}

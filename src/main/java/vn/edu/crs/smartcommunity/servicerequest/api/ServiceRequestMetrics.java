package vn.edu.crs.smartcommunity.servicerequest.api;

import java.util.Map;

public record ServiceRequestMetrics(
        long total, long open, long assigned, long inProgress, long resolved,
        long closed, long cancelled, long overdue, Map<String, Long> requestsByCategory,
        long averageResolutionMinutes) {
}

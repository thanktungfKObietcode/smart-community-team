package vn.edu.crs.smartcommunity.reporting.internal.dto;

import java.time.Instant;
import java.util.List;

public record TechnicianDashboardResponse(
        long assignedTasks, long inProgressTasks, long resolvedTasks,
        long overdueAssignedTasks, List<TechnicianTask> recentTasks) {

    public record TechnicianTask(Long id, String code, String title, String category, String priority,
            String status, Instant dueAt, boolean overdue, Instant createdAt) { }
}

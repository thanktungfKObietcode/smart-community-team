package vn.edu.crs.smartcommunity.servicerequest.internal.dto;

import java.time.Instant;

import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestPriority;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestStatus;

public record ServiceRequestResponse(
        Long id,
        String code,
        Long residentId,
        Long residentUserId,
        Long apartmentId,
        String title,
        String description,
        String category,
        ServiceRequestPriority priority,
        ServiceRequestStatus status,
        Long assignedTechnicianId,
        String resolutionNote,
        Instant createdAt,
        Instant updatedAt,
        Instant assignedAt,
        Instant startedAt,
        Instant resolvedAt,
        Instant closedAt,
        Instant dueAt,
        boolean overdue
) {
}

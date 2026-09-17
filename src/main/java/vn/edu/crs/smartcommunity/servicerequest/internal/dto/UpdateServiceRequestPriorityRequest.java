package vn.edu.crs.smartcommunity.servicerequest.internal.dto;

import jakarta.validation.constraints.NotNull;

import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestPriority;

public record UpdateServiceRequestPriorityRequest(@NotNull ServiceRequestPriority priority) {
}

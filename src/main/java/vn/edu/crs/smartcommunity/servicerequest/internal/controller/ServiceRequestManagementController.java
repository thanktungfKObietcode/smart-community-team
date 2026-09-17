package vn.edu.crs.smartcommunity.servicerequest.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.servicerequest.internal.dto.AssignServiceRequestRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.dto.ServiceRequestResponse;
import vn.edu.crs.smartcommunity.servicerequest.internal.dto.UpdateServiceRequestPriorityRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.service.ServiceRequestService;

@RestController
@RequestMapping("/api/management/service-requests")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ServiceRequestManagementController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestManagementController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @GetMapping
    public List<ServiceRequestResponse> list() {
        return serviceRequestService.listForManagement();
    }

    @PatchMapping("/{requestId}/assign")
    public ServiceRequestResponse assign(
            @PathVariable Long requestId,
            @Valid @RequestBody AssignServiceRequestRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return serviceRequestService.assign(requestId, request, userId(jwt));
    }

    @PatchMapping("/{requestId}/priority")
    public ServiceRequestResponse priority(
            @PathVariable Long requestId,
            @Valid @RequestBody UpdateServiceRequestPriorityRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return serviceRequestService.changePriority(requestId, request.priority(), userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new IllegalStateException("Authenticated token has no userId claim");
        }
        return userId.longValue();
    }
}

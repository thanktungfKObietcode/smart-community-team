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

import vn.edu.crs.smartcommunity.servicerequest.internal.dto.ResolveServiceRequestRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.dto.ServiceRequestResponse;
import vn.edu.crs.smartcommunity.servicerequest.internal.service.ServiceRequestService;

@RestController
@RequestMapping("/api/technician/service-requests")
@PreAuthorize("hasRole('TECHNICIAN')")
public class ServiceRequestTechnicianController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestTechnicianController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @GetMapping("/my")
    public List<ServiceRequestResponse> myRequests(@AuthenticationPrincipal Jwt jwt) {
        return serviceRequestService.listForTechnician(userId(jwt));
    }

    @PatchMapping("/{requestId}/start")
    public ServiceRequestResponse start(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {
        return serviceRequestService.start(requestId, userId(jwt));
    }

    @PatchMapping("/{requestId}/resolve")
    public ServiceRequestResponse resolve(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ResolveServiceRequestRequest request) {
        return serviceRequestService.resolve(requestId, userId(jwt), request);
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new IllegalStateException("Authenticated token has no userId claim");
        }
        return userId.longValue();
    }
}

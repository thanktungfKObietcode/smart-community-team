package vn.edu.crs.smartcommunity.servicerequest.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.servicerequest.internal.dto.CreateServiceRequestRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.dto.ServiceRequestResponse;
import vn.edu.crs.smartcommunity.servicerequest.internal.service.ServiceRequestService;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RESIDENT')")
    public ServiceRequestResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateServiceRequestRequest request) {
        return serviceRequestService.create(userId(jwt), request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RESIDENT')")
    public List<ServiceRequestResponse> myRequests(@AuthenticationPrincipal Jwt jwt) {
        return serviceRequestService.listForResident(userId(jwt));
    }

    @GetMapping("/{requestId}")
    public ServiceRequestResponse get(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {
        return serviceRequestService.getVisible(
                requestId,
                userId(jwt),
                jwt.getClaimAsStringList("roles"));
    }

    @PatchMapping("/{requestId}/confirm")
    @PreAuthorize("hasRole('RESIDENT')")
    public ServiceRequestResponse confirm(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {
        return serviceRequestService.close(requestId, userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new IllegalStateException("Authenticated token has no userId claim");
        }
        return userId.longValue();
    }
}

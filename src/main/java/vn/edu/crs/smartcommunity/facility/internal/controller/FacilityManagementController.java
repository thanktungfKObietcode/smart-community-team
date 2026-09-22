package vn.edu.crs.smartcommunity.facility.internal.controller;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.facility.internal.dto.CreateFacilityRequest;
import vn.edu.crs.smartcommunity.facility.internal.dto.FacilityResponse;
import vn.edu.crs.smartcommunity.facility.internal.dto.UpdateFacilityRequest;
import vn.edu.crs.smartcommunity.facility.internal.service.FacilityService;

@RestController
@RequestMapping("/api/management/facilities")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class FacilityManagementController {

    private final FacilityService facilityService;

    public FacilityManagementController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @PostMapping
    public FacilityResponse create(@Valid @RequestBody CreateFacilityRequest request, @AuthenticationPrincipal Jwt jwt) {
        return facilityService.create(request, userId(jwt));
    }

    @PatchMapping("/{id}")
    public FacilityResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFacilityRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return facilityService.update(id, request, userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) throw new IllegalStateException("Authenticated token has no userId claim");
        return userId.longValue();
    }
}

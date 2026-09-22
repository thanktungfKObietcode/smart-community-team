package vn.edu.crs.smartcommunity.resident.internal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.resident.internal.dto.ResidentResponse;
import vn.edu.crs.smartcommunity.resident.internal.service.ResidentService;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResidentResponse getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new IllegalStateException("Authenticated token has no userId claim");
        }
        return residentService.getMyProfile(userId.longValue());
    }
}

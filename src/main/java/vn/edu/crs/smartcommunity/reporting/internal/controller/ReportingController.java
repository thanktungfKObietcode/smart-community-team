package vn.edu.crs.smartcommunity.reporting.internal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.reporting.internal.dto.ManagerDashboardResponse;
import vn.edu.crs.smartcommunity.reporting.internal.dto.ResidentDashboardResponse;
import vn.edu.crs.smartcommunity.reporting.internal.dto.TechnicianDashboardResponse;
import vn.edu.crs.smartcommunity.reporting.internal.service.ReportingService;

@RestController
public class ReportingController {

    private final ReportingService service;

    public ReportingController(ReportingService service) { this.service = service; }

    @GetMapping("/api/management/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ManagerDashboardResponse managerDashboard() { return service.managerDashboard(); }

    @GetMapping("/api/resident/dashboard")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResidentDashboardResponse residentDashboard(@AuthenticationPrincipal Jwt jwt) {
        return service.residentDashboard(userId(jwt));
    }

    @GetMapping("/api/technician/dashboard")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public TechnicianDashboardResponse technicianDashboard(@AuthenticationPrincipal Jwt jwt) {
        return service.technicianDashboard(userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) throw new IllegalStateException("Authenticated token has no userId claim");
        return userId.longValue();
    }
}

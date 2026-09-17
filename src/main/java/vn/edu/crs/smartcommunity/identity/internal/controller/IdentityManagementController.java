package vn.edu.crs.smartcommunity.identity.internal.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.identity.internal.dto.TechnicianResponse;

@RestController
@RequestMapping("/api/management/technicians")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class IdentityManagementController {

    private final IdentityLookup identityLookup;

    public IdentityManagementController(IdentityLookup identityLookup) {
        this.identityLookup = identityLookup;
    }

    @GetMapping
    public List<TechnicianResponse> technicians() {
        return identityLookup.findActiveTechnicians().stream()
                .map(user -> new TechnicianResponse(user.userId(), user.fullName(), user.email(), user.active()))
                .toList();
    }
}

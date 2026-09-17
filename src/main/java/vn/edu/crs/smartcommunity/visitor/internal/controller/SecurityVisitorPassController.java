package vn.edu.crs.smartcommunity.visitor.internal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.visitor.internal.dto.VisitorPassResponse;
import vn.edu.crs.smartcommunity.visitor.internal.service.VisitorPassService;

@RestController
@RequestMapping("/api/security/visitor-passes")
@PreAuthorize("hasRole('SECURITY')")
public class SecurityVisitorPassController {

    private final VisitorPassService visitorPassService;

    public SecurityVisitorPassController(VisitorPassService visitorPassService) { this.visitorPassService = visitorPassService; }

    @GetMapping("/{code}")
    public VisitorPassResponse verify(@PathVariable String code) { return visitorPassService.verify(code); }

    @PostMapping("/{code}/check-in")
    public VisitorPassResponse checkIn(@PathVariable String code, @AuthenticationPrincipal Jwt jwt) {
        return visitorPassService.checkIn(code, userId(jwt));
    }

    @PostMapping("/{code}/check-out")
    public VisitorPassResponse checkOut(@PathVariable String code, @AuthenticationPrincipal Jwt jwt) {
        return visitorPassService.checkOut(code, userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) throw new IllegalStateException("Authenticated token has no userId claim");
        return userId.longValue();
    }
}

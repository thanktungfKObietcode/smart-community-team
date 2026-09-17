package vn.edu.crs.smartcommunity.visitor.internal.controller;

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

import vn.edu.crs.smartcommunity.visitor.internal.dto.CreateVisitorPassRequest;
import vn.edu.crs.smartcommunity.visitor.internal.dto.VisitorPassResponse;
import vn.edu.crs.smartcommunity.visitor.internal.service.VisitorPassService;

@RestController
@RequestMapping("/api/visitor-passes")
@PreAuthorize("hasRole('RESIDENT')")
public class VisitorPassController {

    private final VisitorPassService visitorPassService;

    public VisitorPassController(VisitorPassService visitorPassService) { this.visitorPassService = visitorPassService; }

    @PostMapping
    public VisitorPassResponse create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateVisitorPassRequest request) {
        return visitorPassService.create(userId(jwt), request);
    }

    @GetMapping("/my")
    public List<VisitorPassResponse> my(@AuthenticationPrincipal Jwt jwt) {
        return visitorPassService.listMine(userId(jwt));
    }

    @GetMapping("/{id}")
    public VisitorPassResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return visitorPassService.getMine(id, userId(jwt));
    }

    @PatchMapping("/{id}/cancel")
    public VisitorPassResponse cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return visitorPassService.cancel(id, userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) throw new IllegalStateException("Authenticated token has no userId claim");
        return userId.longValue();
    }
}

package vn.edu.crs.smartcommunity.audit.internal.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.audit.api.AuditInfo;
import vn.edu.crs.smartcommunity.audit.internal.service.AuditLogService;

@RestController
@RequestMapping("/api/management/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AuditLogController {

    private final AuditLogService service;

    public AuditLogController(AuditLogService service) { this.service = service; }

    @GetMapping
    public List<AuditInfo> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long actorUserId) {
        return service.search(action, entityType, actorUserId);
    }
}

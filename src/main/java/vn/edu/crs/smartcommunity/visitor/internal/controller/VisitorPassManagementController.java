package vn.edu.crs.smartcommunity.visitor.internal.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.visitor.internal.dto.VisitorPassResponse;
import vn.edu.crs.smartcommunity.visitor.internal.service.VisitorPassService;

@RestController
@RequestMapping("/api/management/visitor-passes")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class VisitorPassManagementController {

    private final VisitorPassService visitorPassService;

    public VisitorPassManagementController(VisitorPassService visitorPassService) {
        this.visitorPassService = visitorPassService;
    }

    @GetMapping
    public List<VisitorPassResponse> list() {
        return visitorPassService.listManagement();
    }
}

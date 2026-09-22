package vn.edu.crs.smartcommunity.resident.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.resident.internal.dto.CreateResidentRequest;
import vn.edu.crs.smartcommunity.resident.internal.dto.ResidentResponse;
import vn.edu.crs.smartcommunity.resident.internal.service.ResidentService;

@RestController
@RequestMapping("/api/management/residents")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ResidentManagementController {

    private final ResidentService residentService;

    public ResidentManagementController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @PostMapping
    public ResidentResponse createResident(@Valid @RequestBody CreateResidentRequest request) {
        return residentService.createResident(request);
    }

    @GetMapping
    public List<ResidentResponse> listResidents() {
        return residentService.listResidents();
    }
}

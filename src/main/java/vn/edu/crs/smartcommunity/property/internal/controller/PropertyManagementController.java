package vn.edu.crs.smartcommunity.property.internal.controller;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.property.internal.dto.ApartmentResponse;
import vn.edu.crs.smartcommunity.property.internal.dto.BuildingResponse;
import vn.edu.crs.smartcommunity.property.internal.dto.CreateApartmentRequest;
import vn.edu.crs.smartcommunity.property.internal.dto.CreateBuildingRequest;
import vn.edu.crs.smartcommunity.property.internal.service.PropertyService;

@RestController
@RequestMapping("/api/management")
public class PropertyManagementController {

    private final PropertyService propertyService;

    public PropertyManagementController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping("/buildings")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public BuildingResponse createBuilding(@Valid @RequestBody CreateBuildingRequest request) {
        return propertyService.createBuilding(request);
    }

    @PostMapping("/buildings/{buildingId}/apartments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApartmentResponse createApartment(
            @PathVariable Long buildingId,
            @Valid @RequestBody CreateApartmentRequest request) {
        return propertyService.createApartment(buildingId, request);
    }
}

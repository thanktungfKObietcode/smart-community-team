package vn.edu.crs.smartcommunity.property.internal.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.property.internal.dto.BuildingResponse;
import vn.edu.crs.smartcommunity.property.internal.dto.ApartmentResponse;
import vn.edu.crs.smartcommunity.property.internal.service.PropertyService;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    private final PropertyService propertyService;

    public BuildingController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public List<BuildingResponse> listBuildings() {
        return propertyService.listActiveBuildings();
    }

    @GetMapping("/{buildingId}")
    public BuildingResponse getBuilding(@PathVariable Long buildingId) {
        return propertyService.getActiveBuilding(buildingId);
    }

    @GetMapping("/{buildingId}/apartments")
    public List<ApartmentResponse> listApartments(@PathVariable Long buildingId) {
        return propertyService.listActiveApartments(buildingId);
    }
}

package vn.edu.crs.smartcommunity.property.internal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.property.internal.dto.ApartmentResponse;
import vn.edu.crs.smartcommunity.property.internal.service.PropertyService;

@RestController
@RequestMapping("/api/apartments")
public class ApartmentController {

    private final PropertyService propertyService;

    public ApartmentController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/{apartmentId}")
    public ApartmentResponse getApartment(@PathVariable Long apartmentId) {
        return propertyService.getActiveApartment(apartmentId);
    }
}

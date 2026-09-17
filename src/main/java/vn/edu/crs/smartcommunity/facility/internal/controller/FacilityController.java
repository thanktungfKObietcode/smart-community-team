package vn.edu.crs.smartcommunity.facility.internal.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.facility.internal.dto.FacilityResponse;
import vn.edu.crs.smartcommunity.facility.internal.service.FacilityService;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public List<FacilityResponse> list() { return facilityService.listActive(); }

    @GetMapping("/{id}")
    public FacilityResponse get(@PathVariable Long id) { return facilityService.getActive(id); }
}

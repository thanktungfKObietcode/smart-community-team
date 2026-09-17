package vn.edu.crs.smartcommunity.facility.internal.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.facility.api.FacilityInfo;
import vn.edu.crs.smartcommunity.facility.api.FacilityLookup;
import vn.edu.crs.smartcommunity.facility.internal.repository.FacilityRepository;

@Service
public class FacilityLookupAdapter implements FacilityLookup {

    private final FacilityRepository facilityRepository;

    public FacilityLookupAdapter(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FacilityInfo> getFacility(Long facilityId) {
        return facilityRepository.findById(facilityId).map(facility -> new FacilityInfo(
                facility.getId(), facility.getCode(), facility.getName(), facility.getBuildingId(),
                facility.getStatus(), facility.isBookable(), facility.getOpeningTime(),
                facility.getClosingTime(), facility.isActive()));
    }
}

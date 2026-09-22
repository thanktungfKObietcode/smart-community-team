package vn.edu.crs.smartcommunity.facility.internal.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.facility.api.FacilityMetrics;
import vn.edu.crs.smartcommunity.facility.api.FacilityReporting;
import vn.edu.crs.smartcommunity.facility.api.FacilityStatus;
import vn.edu.crs.smartcommunity.facility.internal.repository.FacilityRepository;

@Component
public class FacilityReportingAdapter implements FacilityReporting {

    private final FacilityRepository repository;

    public FacilityReportingAdapter(FacilityRepository repository) { this.repository = repository; }

    @Override
    @Transactional(readOnly = true)
    public FacilityMetrics getMetrics() {
        var facilities = repository.findByActiveTrueOrderByCodeAsc();
        return new FacilityMetrics(facilities.size(), facilities.stream()
                .filter(facility -> facility.getStatus() == FacilityStatus.AVAILABLE).count(), facilities.stream()
                .filter(facility -> facility.getStatus() == FacilityStatus.MAINTENANCE).count());
    }
}

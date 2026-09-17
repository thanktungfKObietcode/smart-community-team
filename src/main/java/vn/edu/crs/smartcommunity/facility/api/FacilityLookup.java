package vn.edu.crs.smartcommunity.facility.api;

import java.util.Optional;

public interface FacilityLookup {

    Optional<FacilityInfo> getFacility(Long facilityId);
}

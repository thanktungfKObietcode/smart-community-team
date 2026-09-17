package vn.edu.crs.smartcommunity.resident.api;

import java.util.Optional;

public interface ResidentLookup {

    Optional<ResidentInfo> getById(Long residentId);

    Optional<ResidentInfo> getByUserId(Long userId);

    long countActive();
}

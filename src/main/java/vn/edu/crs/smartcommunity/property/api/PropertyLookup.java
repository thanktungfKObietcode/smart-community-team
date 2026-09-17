package vn.edu.crs.smartcommunity.property.api;

import java.util.Optional;

public interface PropertyLookup {

    Optional<BuildingInfo> getBuilding(Long buildingId);

    Optional<BuildingInfo> getBuildingByCode(String code);

    Optional<ApartmentInfo> getApartment(Long apartmentId);

    Optional<ApartmentInfo> getApartment(String buildingCode, String unitNumber);

    boolean apartmentExists(Long apartmentId);
}

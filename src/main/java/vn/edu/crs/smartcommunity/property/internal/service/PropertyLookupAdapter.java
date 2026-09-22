package vn.edu.crs.smartcommunity.property.internal.service;

import java.util.Optional;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.property.api.ApartmentInfo;
import vn.edu.crs.smartcommunity.property.api.BuildingInfo;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;
import vn.edu.crs.smartcommunity.property.internal.entity.Apartment;
import vn.edu.crs.smartcommunity.property.internal.entity.Building;
import vn.edu.crs.smartcommunity.property.internal.repository.ApartmentRepository;
import vn.edu.crs.smartcommunity.property.internal.repository.BuildingRepository;

@Service
public class PropertyLookupAdapter implements PropertyLookup {

    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;

    public PropertyLookupAdapter(
            ApartmentRepository apartmentRepository,
            BuildingRepository buildingRepository) {
        this.apartmentRepository = apartmentRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BuildingInfo> getBuilding(Long buildingId) {
        return buildingRepository.findByIdAndActiveTrue(buildingId)
                .map(building -> new BuildingInfo(
                        building.getId(), building.getCode(), building.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BuildingInfo> getBuildingByCode(String code) {
        return buildingRepository.findByCodeIgnoreCase(code.trim().toUpperCase(Locale.ROOT))
                .filter(building -> building.isActive())
                .map(building -> new BuildingInfo(
                        building.getId(), building.getCode(), building.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApartmentInfo> getApartment(Long apartmentId) {
        return apartmentRepository.findByIdAndActiveTrue(apartmentId).map(this::toInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApartmentInfo> getApartment(String buildingCode, String unitNumber) {
        return apartmentRepository.findByBuildingCodeIgnoreCaseAndUnitNumberIgnoreCase(
                        buildingCode.trim(), unitNumber.trim())
                .filter(apartment -> apartment.isActive() && apartment.getBuilding().isActive())
                .map(this::toInfo);
    }

    @Override
    public boolean apartmentExists(Long apartmentId) {
        return getApartment(apartmentId).isPresent();
    }

    private ApartmentInfo toInfo(Apartment apartment) {
        Building building = apartment.getBuilding();
        return new ApartmentInfo(
                apartment.getId(),
                apartment.getUnitNumber(),
                apartment.getFloorNumber(),
                building.getId(),
                building.getCode(),
                building.getName());
    }
}

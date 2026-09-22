package vn.edu.crs.smartcommunity.property.internal.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.common.error.ConflictException;
import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.property.api.ApartmentInfo;
import vn.edu.crs.smartcommunity.property.internal.dto.ApartmentResponse;
import vn.edu.crs.smartcommunity.property.internal.dto.BuildingResponse;
import vn.edu.crs.smartcommunity.property.internal.dto.CreateApartmentRequest;
import vn.edu.crs.smartcommunity.property.internal.dto.CreateBuildingRequest;
import vn.edu.crs.smartcommunity.property.internal.entity.Apartment;
import vn.edu.crs.smartcommunity.property.internal.entity.Building;
import vn.edu.crs.smartcommunity.property.internal.repository.ApartmentRepository;
import vn.edu.crs.smartcommunity.property.internal.repository.BuildingRepository;

@Service
public class PropertyService {

    private final BuildingRepository buildingRepository;
    private final ApartmentRepository apartmentRepository;

    public PropertyService(
            BuildingRepository buildingRepository,
            ApartmentRepository apartmentRepository) {
        this.buildingRepository = buildingRepository;
        this.apartmentRepository = apartmentRepository;
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> listActiveBuildings() {
        return buildingRepository.findByActiveTrueOrderByCodeAsc().stream()
                .map(this::toBuildingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BuildingResponse getActiveBuilding(Long buildingId) {
        return buildingRepository.findByIdAndActiveTrue(buildingId)
                .map(this::toBuildingResponse)
                .orElseThrow(() -> new NotFoundException("Building not found"));
    }

    @Transactional(readOnly = true)
    public List<ApartmentResponse> listActiveApartments(Long buildingId) {
        requireActiveBuilding(buildingId);
        return apartmentRepository.findByBuildingIdAndActiveTrueOrderByUnitNumberAsc(buildingId).stream()
                .map(this::toApartmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApartmentResponse getActiveApartment(Long apartmentId) {
        return apartmentRepository.findByIdAndActiveTrue(apartmentId)
                .map(this::toApartmentResponse)
                .orElseThrow(() -> new NotFoundException("Apartment not found"));
    }

    @Transactional
    public BuildingResponse createBuilding(CreateBuildingRequest request) {
        String code = normalize(request.code());
        if (buildingRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Building code already exists");
        }

        Building building = new Building();
        building.setCode(code);
        building.setName(request.name().trim());
        building.setAddress(request.address().trim());
        building.setActive(true);
        return toBuildingResponse(buildingRepository.saveAndFlush(building));
    }

    @Transactional
    public ApartmentResponse createApartment(Long buildingId, CreateApartmentRequest request) {
        Building building = requireActiveBuilding(buildingId);
        String unitNumber = normalize(request.unitNumber());
        if (apartmentRepository.existsByBuildingIdAndUnitNumberIgnoreCase(buildingId, unitNumber)) {
            throw new ConflictException("Apartment unit already exists in this building");
        }

        Apartment apartment = new Apartment();
        apartment.setBuilding(building);
        apartment.setUnitNumber(unitNumber);
        apartment.setFloorNumber(request.floorNumber());
        apartment.setActive(true);
        return toApartmentResponse(apartmentRepository.saveAndFlush(apartment));
    }

    @Transactional(readOnly = true)
    public ApartmentInfo getApartmentInfo(Long apartmentId) {
        return apartmentRepository.findByIdAndActiveTrue(apartmentId)
                .map(this::toApartmentInfo)
                .orElseThrow(() -> new NotFoundException("Apartment not found"));
    }

    private Building requireActiveBuilding(Long buildingId) {
        return buildingRepository.findByIdAndActiveTrue(buildingId)
                .orElseThrow(() -> new NotFoundException("Building not found"));
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private BuildingResponse toBuildingResponse(Building building) {
        return new BuildingResponse(
                building.getId(),
                building.getCode(),
                building.getName(),
                building.getAddress(),
                building.isActive(),
                building.getCreatedAt(),
                building.getUpdatedAt());
    }

    private ApartmentResponse toApartmentResponse(Apartment apartment) {
        Building building = apartment.getBuilding();
        return new ApartmentResponse(
                apartment.getId(),
                apartment.getUnitNumber(),
                apartment.getFloorNumber(),
                apartment.isActive(),
                building.getId(),
                building.getCode(),
                building.getName(),
                apartment.getCreatedAt(),
                apartment.getUpdatedAt());
    }

    private ApartmentInfo toApartmentInfo(Apartment apartment) {
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

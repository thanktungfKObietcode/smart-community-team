package vn.edu.crs.smartcommunity.facility.internal.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.common.error.ConflictException;
import vn.edu.crs.smartcommunity.common.error.BadRequestException;
import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.facility.api.FacilityInfo;
import vn.edu.crs.smartcommunity.facility.api.FacilityCreatedEvent;
import vn.edu.crs.smartcommunity.facility.api.FacilityUpdatedEvent;
import vn.edu.crs.smartcommunity.facility.api.FacilityStatus;
import vn.edu.crs.smartcommunity.facility.internal.dto.CreateFacilityRequest;
import vn.edu.crs.smartcommunity.facility.internal.dto.FacilityResponse;
import vn.edu.crs.smartcommunity.facility.internal.dto.UpdateFacilityRequest;
import vn.edu.crs.smartcommunity.facility.internal.entity.Facility;
import vn.edu.crs.smartcommunity.facility.internal.repository.FacilityRepository;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final PropertyLookup propertyLookup;
    private final ApplicationEventPublisher eventPublisher;

    public FacilityService(FacilityRepository facilityRepository, PropertyLookup propertyLookup,
            ApplicationEventPublisher eventPublisher) {
        this.facilityRepository = facilityRepository;
        this.propertyLookup = propertyLookup;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> listActive() {
        return facilityRepository.findByActiveTrueOrderByCodeAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FacilityResponse getActive(Long id) {
        return toResponse(requireActive(id));
    }

    @Transactional
    public FacilityResponse create(CreateFacilityRequest request, Long actorUserId) {
        String code = normalizeCode(request.code());
        if (facilityRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Facility code already exists");
        }
        validateHours(request.openingTime(), request.closingTime());
        validateBuilding(request.buildingId());
        Facility facility = new Facility();
        facility.setCode(code);
        facility.setName(request.name().trim());
        facility.setDescription(normalizeOptional(request.description()));
        facility.setBuildingId(request.buildingId());
        facility.setType(request.type());
        facility.setStatus(FacilityStatus.AVAILABLE);
        facility.setBookable(request.bookable() == null || request.bookable());
        facility.setOpeningTime(request.openingTime());
        facility.setClosingTime(request.closingTime());
        facility.setActive(true);
        FacilityResponse response = toResponse(facilityRepository.saveAndFlush(facility));
        eventPublisher.publishEvent(new FacilityCreatedEvent(response.id(), response.code(), response.name(), actorUserId));
        return response;
    }

    @Transactional
    public FacilityResponse update(Long id, UpdateFacilityRequest request, Long actorUserId) {
        Facility facility = require(id);
        if (request.name() != null) facility.setName(request.name().trim());
        if (request.description() != null) facility.setDescription(normalizeOptional(request.description()));
        if (request.buildingId() != null) {
            validateBuilding(request.buildingId());
            facility.setBuildingId(request.buildingId());
        }
        if (request.type() != null) facility.setType(request.type());
        if (request.status() != null) facility.setStatus(request.status());
        if (request.bookable() != null) facility.setBookable(request.bookable());
        if (request.openingTime() != null) facility.setOpeningTime(request.openingTime());
        if (request.closingTime() != null) facility.setClosingTime(request.closingTime());
        if (request.active() != null) facility.setActive(request.active());
        validateHours(facility.getOpeningTime(), facility.getClosingTime());
        FacilityResponse response = toResponse(facilityRepository.saveAndFlush(facility));
        eventPublisher.publishEvent(new FacilityUpdatedEvent(response.id(), response.code(), response.name(), actorUserId));
        return response;
    }

    @Transactional(readOnly = true)
    public FacilityInfo getInfo(Long id) {
        Facility facility = require(id);
        return toInfo(facility);
    }

    private Facility require(Long id) {
        return facilityRepository.findById(id).orElseThrow(() -> new NotFoundException("Facility not found"));
    }

    private Facility requireActive(Long id) {
        return facilityRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Facility not found"));
    }

    private void validateBuilding(Long buildingId) {
        if (buildingId != null && propertyLookup.getBuilding(buildingId).isEmpty()) {
            throw new NotFoundException("Building not found or inactive");
        }
    }

    private void validateHours(LocalTime openingTime, LocalTime closingTime) {
        if (openingTime == null || closingTime == null || !openingTime.isBefore(closingTime)) {
            throw new BadRequestException("Facility opening time must be before closing time");
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private FacilityInfo toInfo(Facility facility) {
        return new FacilityInfo(facility.getId(), facility.getCode(), facility.getName(), facility.getBuildingId(),
                facility.getStatus(), facility.isBookable(), facility.getOpeningTime(),
                facility.getClosingTime(), facility.isActive());
    }

    private FacilityResponse toResponse(Facility facility) {
        return new FacilityResponse(facility.getId(), facility.getCode(), facility.getName(), facility.getDescription(),
                facility.getBuildingId(), facility.getType(), facility.getStatus(), facility.isBookable(),
                facility.getOpeningTime(), facility.getClosingTime(), facility.isActive(), facility.getCreatedAt(),
                facility.getUpdatedAt());
    }
}

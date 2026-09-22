package vn.edu.crs.smartcommunity.visitor.internal.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.common.error.ConflictException;
import vn.edu.crs.smartcommunity.common.error.BadRequestException;
import vn.edu.crs.smartcommunity.common.error.ForbiddenException;
import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.property.api.ApartmentInfo;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;
import vn.edu.crs.smartcommunity.resident.api.ResidentInfo;
import vn.edu.crs.smartcommunity.resident.api.ResidentLookup;
import vn.edu.crs.smartcommunity.visitor.api.VisitorCheckedInEvent;
import vn.edu.crs.smartcommunity.visitor.api.VisitorCheckedOutEvent;
import vn.edu.crs.smartcommunity.visitor.api.VisitorPassCreatedEvent;
import vn.edu.crs.smartcommunity.visitor.api.VisitorPassCancelledEvent;
import vn.edu.crs.smartcommunity.visitor.internal.dto.CreateVisitorPassRequest;
import vn.edu.crs.smartcommunity.visitor.internal.dto.VisitorPassResponse;
import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPass;
import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPassStatus;
import vn.edu.crs.smartcommunity.visitor.internal.repository.VisitorPassRepository;

@Service
public class VisitorPassService {

    private static final Duration MAX_VALIDITY = Duration.ofHours(24);

    private final VisitorPassRepository visitorPassRepository;
    private final ResidentLookup residentLookup;
    private final PropertyLookup propertyLookup;
    private final ApplicationEventPublisher eventPublisher;
    private final VisitorPassExpirationService expirationService;

    public VisitorPassService(VisitorPassRepository visitorPassRepository, ResidentLookup residentLookup,
            PropertyLookup propertyLookup, ApplicationEventPublisher eventPublisher,
            VisitorPassExpirationService expirationService) {
        this.visitorPassRepository = visitorPassRepository;
        this.residentLookup = residentLookup;
        this.propertyLookup = propertyLookup;
        this.eventPublisher = eventPublisher;
        this.expirationService = expirationService;
    }

    @Transactional
    public VisitorPassResponse create(Long userId, CreateVisitorPassRequest request) {
        ResidentInfo resident = residentLookup.getByUserId(userId).filter(ResidentInfo::active)
                .orElseThrow(() -> new NotFoundException("Active resident profile not found"));
        LocalDateTime now = LocalDateTime.now();
        if (!request.validFrom().isBefore(request.validUntil())) {
            throw new BadRequestException("Visitor pass validFrom must be before validUntil");
        }
        if (!request.validUntil().isAfter(now)) throw new BadRequestException("Visitor pass must be in the future");
        if (Duration.between(request.validFrom(), request.validUntil()).compareTo(MAX_VALIDITY) > 0) {
            throw new BadRequestException("Visitor pass validity cannot exceed 24 hours");
        }
        ApartmentInfo apartment = propertyLookup.getApartment(resident.apartmentId())
                .orElseThrow(() -> new NotFoundException("Apartment not found or inactive"));
        VisitorPass pass = new VisitorPass();
        pass.setCode(generateCode());
        pass.setResidentId(resident.residentId());
        pass.setApartmentId(apartment.id());
        pass.setVisitorName(request.visitorName().trim());
        pass.setVisitorPhone(normalizeOptional(request.visitorPhone()));
        pass.setValidFrom(request.validFrom());
        pass.setValidUntil(request.validUntil());
        pass.setStatus(VisitorPassStatus.ACTIVE);
        VisitorPass saved = visitorPassRepository.saveAndFlush(pass);
        eventPublisher.publishEvent(new VisitorPassCreatedEvent(saved.getId(), userId, saved.getCode(),
                saved.getVisitorName(), saved.getValidFrom(), saved.getValidUntil()));
        return toResponse(saved, apartment);
    }

    @Transactional
    public List<VisitorPassResponse> listMine(Long userId) {
        ResidentInfo resident = residentLookup.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Resident profile not found"));
        return visitorPassRepository.findByResidentIdOrderByValidFromDesc(resident.residentId()).stream()
                .map(this::expireAndMap).toList();
    }

    @Transactional
    public VisitorPassResponse getMine(Long id, Long userId) {
        ResidentInfo resident = residentLookup.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Resident profile not found"));
        VisitorPass pass = visitorPassRepository.findByIdAndResidentId(id, resident.residentId())
                .orElseThrow(() -> new NotFoundException("Visitor pass not found"));
        return expireAndMap(pass);
    }

    @Transactional
    public VisitorPassResponse cancel(Long id, Long userId) {
        ResidentInfo resident = residentLookup.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Resident profile not found"));
        VisitorPass pass = visitorPassRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Visitor pass not found"));
        if (!pass.getResidentId().equals(resident.residentId())) throw new ForbiddenException("You cannot modify this visitor pass");
        expire(pass);
        if (pass.getStatus() != VisitorPassStatus.ACTIVE) throw new ConflictException("Only active visitor passes can be cancelled");
        pass.setStatus(VisitorPassStatus.CANCELLED);
        pass.setCancelledAt(Instant.now());
        VisitorPass saved = visitorPassRepository.saveAndFlush(pass);
        eventPublisher.publishEvent(new VisitorPassCancelledEvent(saved.getId(), userId, saved.getCode(), userId));
        return toResponse(saved);
    }

    @Transactional
    public VisitorPassResponse verify(String code) {
        return toResponse(loadByCode(code));
    }

    @Transactional
    public VisitorPassResponse checkIn(String code, Long actorUserId) {
        VisitorPass pass = loadByCode(code);
        LocalDateTime now = LocalDateTime.now();
        if (pass.getStatus() != VisitorPassStatus.ACTIVE) throw new ConflictException("Visitor pass is not active");
        if (now.isBefore(pass.getValidFrom()) || now.isAfter(pass.getValidUntil())) {
            throw new ConflictException("Visitor pass is outside its validity window");
        }
        pass.setStatus(VisitorPassStatus.CHECKED_IN);
        pass.setCheckedInAt(Instant.now());
        VisitorPass saved = visitorPassRepository.saveAndFlush(pass);
        Long residentUserId = residentUserId(saved);
        eventPublisher.publishEvent(new VisitorCheckedInEvent(saved.getId(), residentUserId, saved.getCode(),
                saved.getVisitorName(), actorUserId));
        return toResponse(saved);
    }

    @Transactional
    public VisitorPassResponse checkOut(String code, Long actorUserId) {
        VisitorPass pass = loadByCode(code);
        if (pass.getStatus() != VisitorPassStatus.CHECKED_IN) throw new ConflictException("Visitor pass is not checked in");
        pass.setStatus(VisitorPassStatus.CHECKED_OUT);
        pass.setCheckedOutAt(Instant.now());
        VisitorPass saved = visitorPassRepository.saveAndFlush(pass);
        eventPublisher.publishEvent(new VisitorCheckedOutEvent(saved.getId(), residentUserId(saved), saved.getCode(), actorUserId));
        return toResponse(saved);
    }

    private VisitorPass loadByCode(String code) {
        VisitorPass pass = visitorPassRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new NotFoundException("Visitor pass not found"));
        expire(pass);
        return pass;
    }

    private void expire(VisitorPass pass) {
        if (pass.getStatus() == VisitorPassStatus.ACTIVE && LocalDateTime.now().isAfter(pass.getValidUntil())) {
            expirationService.expireIfNecessary(pass.getId());
            pass.setStatus(VisitorPassStatus.EXPIRED);
        }
    }

    private VisitorPassResponse expireAndMap(VisitorPass pass) {
        expire(pass);
        return toResponse(pass);
    }

    private Long residentUserId(VisitorPass pass) {
        return residentLookup.getById(pass.getResidentId())
                .map(ResidentInfo::userId).orElseThrow();
    }

    private String generateCode() {
        String code;
        do {
            code = "VP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (visitorPassRepository.findByCodeIgnoreCase(code).isPresent());
        return code;
    }

    private String normalizeOptional(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private VisitorPassResponse toResponse(VisitorPass pass) {
        ApartmentInfo apartment = propertyLookup.getApartment(pass.getApartmentId()).orElse(null);
        return toResponse(pass, apartment);
    }

    private VisitorPassResponse toResponse(VisitorPass pass, ApartmentInfo apartment) {
        return new VisitorPassResponse(pass.getId(), pass.getCode(), pass.getVisitorName(), pass.getVisitorPhone(),
                pass.getValidFrom(), pass.getValidUntil(), pass.getStatus(), pass.getCreatedAt(), pass.getCheckedInAt(),
                pass.getCheckedOutAt(), apartment);
    }
}

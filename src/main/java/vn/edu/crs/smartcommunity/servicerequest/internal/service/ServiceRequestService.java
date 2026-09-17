package vn.edu.crs.smartcommunity.servicerequest.internal.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.common.error.ConflictException;
import vn.edu.crs.smartcommunity.common.error.ForbiddenException;
import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.identity.api.IdentityUserInfo;
import vn.edu.crs.smartcommunity.resident.api.ResidentInfo;
import vn.edu.crs.smartcommunity.resident.api.ResidentLookup;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestAssignedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestClosedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestCreatedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestResolvedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestStartedEvent;
import vn.edu.crs.smartcommunity.servicerequest.internal.dto.AssignServiceRequestRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.dto.CreateServiceRequestRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.dto.ResolveServiceRequestRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.dto.ServiceRequestResponse;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestStatus;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestPriority;
import vn.edu.crs.smartcommunity.servicerequest.internal.repository.ServiceRequestRepository;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ResidentLookup residentLookup;
    private final IdentityLookup identityLookup;
    private final ApplicationEventPublisher eventPublisher;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository,
            ResidentLookup residentLookup,
            IdentityLookup identityLookup,
            ApplicationEventPublisher eventPublisher) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.residentLookup = residentLookup;
        this.identityLookup = identityLookup;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ServiceRequestResponse create(Long residentUserId, CreateServiceRequestRequest request) {
        ResidentInfo resident = residentLookup.getByUserId(residentUserId)
                .filter(ResidentInfo::active)
                .orElseThrow(() -> new NotFoundException("Active resident profile not found"));

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setResidentId(resident.residentId());
        serviceRequest.setResidentUserId(resident.userId());
        serviceRequest.setApartmentId(resident.apartmentId());
        serviceRequest.setTitle(request.title().trim());
        serviceRequest.setDescription(request.description().trim());
        serviceRequest.setCategory(normalizeOptional(request.category()));
        // Resident submissions always start at the normal SLA; management may reprioritize OPEN/ASSIGNED work.
        serviceRequest.setPriority(ServiceRequestPriority.NORMAL);
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);

        ServiceRequest saved = serviceRequestRepository.saveAndFlush(serviceRequest);
        saved.setDueAt(saved.getCreatedAt().plusSeconds(targetSeconds(saved.getPriority())));
        saved = serviceRequestRepository.saveAndFlush(saved);
        eventPublisher.publishEvent(new ServiceRequestCreatedEvent(
                saved.getId(),
                saved.getResidentUserId(),
                saved.getTitle(),
                residentUserId));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> listForResident(Long residentUserId) {
        return serviceRequestRepository.findByResidentUserIdOrderByCreatedAtDesc(residentUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> listForTechnician(Long technicianUserId) {
        return serviceRequestRepository.findByAssignedTechnicianIdOrderByCreatedAtDesc(technicianUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> listForManagement() {
        return serviceRequestRepository.findAll().stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse get(Long requestId) {
        return toResponse(requireRequest(requestId));
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse getVisible(
            Long requestId,
            Long userId,
            Collection<String> roles) {
        ServiceRequest serviceRequest = requireRequest(requestId);
        boolean manager = roles != null
                && (roles.contains("ADMIN") || roles.contains("MANAGER"));
        boolean residentOwner = roles != null
                && roles.contains("RESIDENT")
                && userId.equals(serviceRequest.getResidentUserId());
        boolean assignedTechnician = roles != null
                && roles.contains("TECHNICIAN")
                && userId.equals(serviceRequest.getAssignedTechnicianId());
        if (!manager && !residentOwner && !assignedTechnician) {
            throw new ForbiddenException("You cannot access this service request");
        }
        return toResponse(serviceRequest);
    }

    @Transactional
    public ServiceRequestResponse assign(Long requestId, AssignServiceRequestRequest request, Long actorUserId) {
        IdentityUserInfo technician = identityLookup.getUserById(request.technicianId())
                .orElseThrow(() -> new NotFoundException("Technician user not found"));
        if (!technician.active() || !technician.roles().contains("TECHNICIAN")) {
            throw new ConflictException("User must be an active technician");
        }

        ServiceRequest serviceRequest = requireRequest(requestId);
        requireStatus(serviceRequest, ServiceRequestStatus.OPEN);
        Instant now = Instant.now();
        serviceRequest.setAssignedTechnicianId(technician.userId());
        serviceRequest.setAssignedAt(now);
        serviceRequest.setStatus(ServiceRequestStatus.ASSIGNED);
        ServiceRequest saved = serviceRequestRepository.saveAndFlush(serviceRequest);
        eventPublisher.publishEvent(new ServiceRequestAssignedEvent(
                saved.getId(),
                saved.getAssignedTechnicianId(),
                saved.getTitle(),
                actorUserId));
        return toResponse(saved);
    }

    @Transactional
    public ServiceRequestResponse start(Long requestId, Long technicianUserId) {
        ServiceRequest serviceRequest = requireRequest(requestId);
        requireAssignedTechnician(serviceRequest, technicianUserId);
        requireStatus(serviceRequest, ServiceRequestStatus.ASSIGNED);
        serviceRequest.setStartedAt(Instant.now());
        serviceRequest.setStatus(ServiceRequestStatus.IN_PROGRESS);
        ServiceRequest saved = serviceRequestRepository.saveAndFlush(serviceRequest);
        eventPublisher.publishEvent(new ServiceRequestStartedEvent(
                saved.getId(), saved.getAssignedTechnicianId(), saved.getTitle(), technicianUserId));
        return toResponse(saved);
    }

    @Transactional
    public ServiceRequestResponse resolve(
            Long requestId,
            Long technicianUserId,
            ResolveServiceRequestRequest request) {
        ServiceRequest serviceRequest = requireRequest(requestId);
        requireAssignedTechnician(serviceRequest, technicianUserId);
        requireStatus(serviceRequest, ServiceRequestStatus.IN_PROGRESS);
        serviceRequest.setResolutionNote(request.resolutionNote().trim());
        serviceRequest.setResolvedAt(Instant.now());
        serviceRequest.setStatus(ServiceRequestStatus.RESOLVED);
        ServiceRequest saved = serviceRequestRepository.saveAndFlush(serviceRequest);
        eventPublisher.publishEvent(new ServiceRequestResolvedEvent(
                saved.getId(),
                saved.getResidentUserId(),
                saved.getTitle(),
                technicianUserId));
        return toResponse(saved);
    }

    @Transactional
    public ServiceRequestResponse close(Long requestId, Long residentUserId) {
        ServiceRequest serviceRequest = requireRequest(requestId);
        if (!serviceRequest.getResidentUserId().equals(residentUserId)) {
            throw new ForbiddenException("Only the request resident can close this request");
        }
        requireStatus(serviceRequest, ServiceRequestStatus.RESOLVED);
        serviceRequest.setClosedAt(Instant.now());
        serviceRequest.setStatus(ServiceRequestStatus.CLOSED);
        ServiceRequest saved = serviceRequestRepository.saveAndFlush(serviceRequest);
        eventPublisher.publishEvent(new ServiceRequestClosedEvent(
                saved.getId(),
                saved.getResidentUserId(),
                saved.getTitle(),
                residentUserId));
        return toResponse(saved);
    }

    @Transactional
    public ServiceRequestResponse changePriority(
            Long requestId, ServiceRequestPriority priority, Long actorUserId) {
        ServiceRequest serviceRequest = requireRequest(requestId);
        if (serviceRequest.getStatus() != ServiceRequestStatus.OPEN
                && serviceRequest.getStatus() != ServiceRequestStatus.ASSIGNED) {
            throw new ConflictException("Priority can only be changed while the request is OPEN or ASSIGNED");
        }
        serviceRequest.setPriority(priority);
        serviceRequest.setDueAt(serviceRequest.getCreatedAt().plusSeconds(targetSeconds(priority)));
        return toResponse(serviceRequestRepository.saveAndFlush(serviceRequest));
    }

    private ServiceRequest requireRequest(Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Service request not found"));
    }

    private void requireAssignedTechnician(ServiceRequest serviceRequest, Long technicianUserId) {
        if (!technicianUserId.equals(serviceRequest.getAssignedTechnicianId())) {
            throw new ForbiddenException("Only the assigned technician can update this request");
        }
    }

    private void requireStatus(ServiceRequest serviceRequest, ServiceRequestStatus expected) {
        if (serviceRequest.getStatus() != expected) {
            throw new ConflictException(
                    "Service request must be " + expected + " but is " + serviceRequest.getStatus());
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ServiceRequestResponse toResponse(ServiceRequest serviceRequest) {
        return new ServiceRequestResponse(
                serviceRequest.getId(),
                serviceRequest.getCode(),
                serviceRequest.getResidentId(),
                serviceRequest.getResidentUserId(),
                serviceRequest.getApartmentId(),
                serviceRequest.getTitle(),
                serviceRequest.getDescription(),
                serviceRequest.getCategory(),
                serviceRequest.getPriority(),
                serviceRequest.getStatus(),
                serviceRequest.getAssignedTechnicianId(),
                serviceRequest.getResolutionNote(),
                serviceRequest.getCreatedAt(),
                serviceRequest.getUpdatedAt(),
                serviceRequest.getAssignedAt(),
                serviceRequest.getStartedAt(),
                serviceRequest.getResolvedAt(),
                serviceRequest.getClosedAt(),
                serviceRequest.getDueAt(),
                isOverdue(serviceRequest));
    }

    private boolean isOverdue(ServiceRequest serviceRequest) {
        return serviceRequest.getDueAt() != null
                && Instant.now().isAfter(serviceRequest.getDueAt())
                && serviceRequest.getStatus() != ServiceRequestStatus.RESOLVED
                && serviceRequest.getStatus() != ServiceRequestStatus.CLOSED
                && serviceRequest.getStatus() != ServiceRequestStatus.CANCELLED;
    }

    public static long targetSeconds(ServiceRequestPriority priority) {
        return switch (priority) {
            case LOW -> 72 * 60 * 60L;
            case NORMAL -> 24 * 60 * 60L;
            case HIGH -> 8 * 60 * 60L;
            case CRITICAL -> 2 * 60 * 60L;
        };
    }
}

package vn.edu.crs.smartcommunity.servicerequest.internal.service;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.servicerequest.api.ResidentServiceRequestMetrics;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestLookup;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestMetrics;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestSummary;
import vn.edu.crs.smartcommunity.servicerequest.api.TechnicianServiceRequestMetrics;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequest;
import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequestStatus;
import vn.edu.crs.smartcommunity.servicerequest.internal.repository.ServiceRequestRepository;

@Component
public class ServiceRequestLookupAdapter implements ServiceRequestLookup {

    private final ServiceRequestRepository repository;

    public ServiceRequestLookupAdapter(ServiceRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestMetrics getMetrics() {
        List<ServiceRequest> requests = repository.findAll();
        Map<ServiceRequestStatus, Long> statuses = new EnumMap<>(ServiceRequestStatus.class);
        requests.forEach(request -> statuses.merge(request.getStatus(), 1L, Long::sum));
        long resolvedWithTime = requests.stream().filter(request -> request.getResolvedAt() != null).count();
        long average = resolvedWithTime == 0 ? 0 : Math.round(requests.stream()
                .filter(request -> request.getResolvedAt() != null)
                .mapToLong(request -> Duration.between(request.getCreatedAt(), request.getResolvedAt()).toMinutes())
                .average().orElse(0));
        Map<String, Long> categories = new LinkedHashMap<>();
        requests.stream().map(ServiceRequest::getCategory).filter(category -> category != null && !category.isBlank())
                .sorted().forEach(category -> categories.merge(category, 1L, Long::sum));
        return new ServiceRequestMetrics(requests.size(), count(statuses, ServiceRequestStatus.OPEN),
                count(statuses, ServiceRequestStatus.ASSIGNED), count(statuses, ServiceRequestStatus.IN_PROGRESS),
                count(statuses, ServiceRequestStatus.RESOLVED), count(statuses, ServiceRequestStatus.CLOSED),
                count(statuses, ServiceRequestStatus.CANCELLED), requests.stream().filter(this::overdue).count(),
                categories, average);
    }

    @Override
    @Transactional(readOnly = true)
    public ResidentServiceRequestMetrics getResidentMetrics(Long userId) {
        List<ServiceRequest> requests = repository.findByResidentUserIdOrderByCreatedAtDesc(userId);
        return new ResidentServiceRequestMetrics(count(requests, ServiceRequestStatus.OPEN),
                count(requests, ServiceRequestStatus.IN_PROGRESS), count(requests, ServiceRequestStatus.RESOLVED));
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicianServiceRequestMetrics getTechnicianMetrics(Long userId) {
        List<ServiceRequest> requests = repository.findByAssignedTechnicianIdOrderByCreatedAtDesc(userId);
        return new TechnicianServiceRequestMetrics(countAll(requests), count(requests, ServiceRequestStatus.IN_PROGRESS),
                count(requests, ServiceRequestStatus.RESOLVED), requests.stream().filter(this::overdue).count(),
                requests.stream().limit(10).map(this::summary).toList());
    }

    private long count(Map<ServiceRequestStatus, Long> values, ServiceRequestStatus status) {
        return values.getOrDefault(status, 0L);
    }

    private long count(List<ServiceRequest> requests, ServiceRequestStatus status) {
        return requests.stream().filter(request -> request.getStatus() == status).count();
    }

    private long countAll(List<ServiceRequest> requests) {
        return requests.size();
    }

    private boolean overdue(ServiceRequest request) {
        return request.getDueAt() != null && Instant.now().isAfter(request.getDueAt())
                && request.getStatus() != ServiceRequestStatus.RESOLVED
                && request.getStatus() != ServiceRequestStatus.CLOSED
                && request.getStatus() != ServiceRequestStatus.CANCELLED;
    }

    private ServiceRequestSummary summary(ServiceRequest request) {
        return new ServiceRequestSummary(request.getId(), request.getCode(), request.getTitle(), request.getCategory(),
                request.getPriority().name(), request.getStatus().name(), request.getDueAt(), overdue(request),
                request.getCreatedAt());
    }
}

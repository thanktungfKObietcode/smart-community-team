package vn.edu.crs.smartcommunity.servicerequest.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.servicerequest.internal.entity.ServiceRequest;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByResidentUserIdOrderByCreatedAtDesc(Long residentUserId);

    List<ServiceRequest> findByAssignedTechnicianIdOrderByCreatedAtDesc(Long assignedTechnicianId);

    Optional<ServiceRequest> findByIdAndResidentUserId(Long id, Long residentUserId);

    Optional<ServiceRequest> findByIdAndAssignedTechnicianId(Long id, Long technicianId);
}

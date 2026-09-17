package vn.edu.crs.smartcommunity.servicerequest.api;

public interface ServiceRequestLookup {

    ServiceRequestMetrics getMetrics();

    ResidentServiceRequestMetrics getResidentMetrics(Long userId);

    TechnicianServiceRequestMetrics getTechnicianMetrics(Long userId);
}

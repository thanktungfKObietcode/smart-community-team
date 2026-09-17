package vn.edu.crs.smartcommunity.reporting.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.edu.crs.smartcommunity.audit.api.AuditInfo;
import vn.edu.crs.smartcommunity.audit.api.AuditLookup;
import vn.edu.crs.smartcommunity.booking.api.BookingMetrics;
import vn.edu.crs.smartcommunity.booking.api.BookingReporting;
import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.facility.api.FacilityMetrics;
import vn.edu.crs.smartcommunity.facility.api.FacilityReporting;
import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.identity.api.IdentityUserInfo;
import vn.edu.crs.smartcommunity.notification.api.NotificationLookup;
import vn.edu.crs.smartcommunity.property.api.ApartmentInfo;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;
import vn.edu.crs.smartcommunity.resident.api.ResidentInfo;
import vn.edu.crs.smartcommunity.resident.api.ResidentLookup;
import vn.edu.crs.smartcommunity.servicerequest.api.ResidentServiceRequestMetrics;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestLookup;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestMetrics;
import vn.edu.crs.smartcommunity.servicerequest.api.TechnicianServiceRequestMetrics;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestSummary;
import vn.edu.crs.smartcommunity.visitor.api.VisitorMetrics;
import vn.edu.crs.smartcommunity.visitor.api.VisitorReporting;
import vn.edu.crs.smartcommunity.reporting.internal.dto.ManagerDashboardResponse;
import vn.edu.crs.smartcommunity.reporting.internal.dto.ResidentDashboardResponse;
import vn.edu.crs.smartcommunity.reporting.internal.dto.TechnicianDashboardResponse;

@Service
public class ReportingService {

    private final ServiceRequestLookup serviceRequestLookup;
    private final ResidentLookup residentLookup;
    private final IdentityLookup identityLookup;
    private final PropertyLookup propertyLookup;
    private final FacilityReporting facilityReporting;
    private final BookingReporting bookingReporting;
    private final VisitorReporting visitorReporting;
    private final NotificationLookup notificationLookup;
    private final AuditLookup auditLookup;

    public ReportingService(ServiceRequestLookup serviceRequestLookup, ResidentLookup residentLookup,
            IdentityLookup identityLookup, PropertyLookup propertyLookup, FacilityReporting facilityReporting,
            BookingReporting bookingReporting, VisitorReporting visitorReporting,
            NotificationLookup notificationLookup, AuditLookup auditLookup) {
        this.serviceRequestLookup = serviceRequestLookup;
        this.residentLookup = residentLookup;
        this.identityLookup = identityLookup;
        this.propertyLookup = propertyLookup;
        this.facilityReporting = facilityReporting;
        this.bookingReporting = bookingReporting;
        this.visitorReporting = visitorReporting;
        this.notificationLookup = notificationLookup;
        this.auditLookup = auditLookup;
    }

    public ManagerDashboardResponse managerDashboard() {
        ServiceRequestMetrics requests = serviceRequestLookup.getMetrics();
        FacilityMetrics facilities = facilityReporting.getMetrics();
        BookingMetrics bookings = bookingReporting.getMetrics();
        VisitorMetrics visitors = visitorReporting.getMetrics();
        return new ManagerDashboardResponse(
                new ManagerDashboardResponse.ServiceRequestDashboard(requests.total(), requests.open(), requests.assigned(),
                        requests.inProgress(), requests.resolved(), requests.closed(), requests.cancelled(), requests.overdue(),
                        requests.requestsByCategory(), requests.averageResolutionMinutes()),
                new ManagerDashboardResponse.ResidentDashboard(residentLookup.countActive()),
                new ManagerDashboardResponse.FacilityDashboard(facilities.total(), facilities.available(), facilities.maintenance()),
                new ManagerDashboardResponse.BookingDashboard(bookings.today(), bookings.confirmed()),
                new ManagerDashboardResponse.VisitorDashboard(visitors.activeToday(), visitors.checkedIn()),
                auditLookup.latest(10));
    }

    public ResidentDashboardResponse residentDashboard(Long userId) {
        ResidentInfo resident = residentLookup.getByUserId(userId).filter(ResidentInfo::active)
                .orElseThrow(() -> new NotFoundException("Active resident profile not found"));
        IdentityUserInfo user = identityLookup.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ApartmentInfo apartment = propertyLookup.getApartment(resident.apartmentId())
                .orElseThrow(() -> new NotFoundException("Apartment not found"));
        ResidentServiceRequestMetrics requests = serviceRequestLookup.getResidentMetrics(userId);
        return new ResidentDashboardResponse(
                new ResidentDashboardResponse.ResidentSummary(user.fullName(), apartment.unitNumber(), apartment.buildingCode()),
                new ResidentDashboardResponse.ServiceRequestCounts(requests.open(), requests.inProgress(), requests.resolved()),
                new ResidentDashboardResponse.BookingCounts(bookingReporting.countUpcomingByResidentId(resident.residentId())),
                new ResidentDashboardResponse.VisitorPassCounts(visitorReporting.countActiveByResidentId(resident.residentId())),
                notificationLookup.countUnreadByUserId(userId));
    }

    public TechnicianDashboardResponse technicianDashboard(Long userId) {
        TechnicianServiceRequestMetrics metrics = serviceRequestLookup.getTechnicianMetrics(userId);
        List<TechnicianDashboardResponse.TechnicianTask> tasks = metrics.recentTasks().stream()
                .map(this::toTechnicianTask).toList();
        return new TechnicianDashboardResponse(metrics.assigned(), metrics.inProgress(), metrics.resolved(),
                metrics.overdue(), tasks);
    }

    private TechnicianDashboardResponse.TechnicianTask toTechnicianTask(ServiceRequestSummary task) {
        return new TechnicianDashboardResponse.TechnicianTask(task.id(), task.code(), task.title(), task.category(), task.priority(),
                task.status(), task.dueAt(), task.overdue(), task.createdAt());
    }
}

package vn.edu.crs.smartcommunity.audit.internal.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import vn.edu.crs.smartcommunity.audit.internal.service.AuditLogService;
import vn.edu.crs.smartcommunity.booking.api.BookingCancelledEvent;
import vn.edu.crs.smartcommunity.booking.api.BookingConfirmedEvent;
import vn.edu.crs.smartcommunity.facility.api.FacilityCreatedEvent;
import vn.edu.crs.smartcommunity.facility.api.FacilityUpdatedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestAssignedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestClosedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestCreatedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestResolvedEvent;
import vn.edu.crs.smartcommunity.servicerequest.api.ServiceRequestStartedEvent;
import vn.edu.crs.smartcommunity.visitor.api.VisitorCheckedInEvent;
import vn.edu.crs.smartcommunity.visitor.api.VisitorCheckedOutEvent;
import vn.edu.crs.smartcommunity.visitor.api.VisitorPassCancelledEvent;
import vn.edu.crs.smartcommunity.visitor.api.VisitorPassCreatedEvent;

@Component
public class AuditEventListener {

    private final AuditLogService service;

    public AuditEventListener(AuditLogService service) { this.service = service; }

    @EventListener
    public void created(ServiceRequestCreatedEvent event) {
        service.record(event.actorUserId(), "SERVICE_REQUEST_CREATED", "SERVICE_REQUEST", event.requestId(), event.title());
    }

    @EventListener
    public void assigned(ServiceRequestAssignedEvent event) {
        service.record(event.actorUserId(), "SERVICE_REQUEST_ASSIGNED", "SERVICE_REQUEST", event.requestId(), event.title());
    }

    @EventListener
    public void started(ServiceRequestStartedEvent event) {
        service.record(event.actorUserId(), "SERVICE_REQUEST_STARTED", "SERVICE_REQUEST", event.requestId(), event.title());
    }

    @EventListener
    public void resolved(ServiceRequestResolvedEvent event) {
        service.record(event.actorUserId(), "SERVICE_REQUEST_RESOLVED", "SERVICE_REQUEST", event.requestId(), event.title());
    }

    @EventListener
    public void closed(ServiceRequestClosedEvent event) {
        service.record(event.actorUserId(), "SERVICE_REQUEST_CLOSED", "SERVICE_REQUEST", event.requestId(), event.title());
    }

    @EventListener
    public void confirmed(BookingConfirmedEvent event) {
        service.record(event.residentUserId(), "BOOKING_CONFIRMED", "BOOKING", event.bookingId(), event.facilityName());
    }

    @EventListener
    public void cancelled(BookingCancelledEvent event) {
        service.record(event.residentUserId(), "BOOKING_CANCELLED", "BOOKING", event.bookingId(), "Booking cancelled");
    }

    @EventListener
    public void passCreated(VisitorPassCreatedEvent event) {
        service.record(event.residentUserId(), "VISITOR_PASS_CREATED", "VISITOR_PASS", event.passId(), event.code());
    }

    @EventListener
    public void checkedIn(VisitorCheckedInEvent event) {
        service.record(event.actorUserId(), "VISITOR_CHECKED_IN", "VISITOR_PASS", event.passId(), event.code());
    }

    @EventListener
    public void checkedOut(VisitorCheckedOutEvent event) {
        service.record(event.actorUserId(), "VISITOR_CHECKED_OUT", "VISITOR_PASS", event.passId(), event.code());
    }

    @EventListener
    public void passCancelled(VisitorPassCancelledEvent event) {
        service.record(event.actorUserId(), "VISITOR_CANCELLED", "VISITOR_PASS", event.passId(), event.code());
    }

    @EventListener
    public void facilityCreated(FacilityCreatedEvent event) {
        service.record(event.actorUserId(), "FACILITY_CREATED", "FACILITY", event.facilityId(), event.name());
    }

    @EventListener
    public void facilityUpdated(FacilityUpdatedEvent event) {
        service.record(event.actorUserId(), "FACILITY_UPDATED", "FACILITY", event.facilityId(), event.name());
    }
}

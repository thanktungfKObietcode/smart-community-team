package vn.edu.crs.smartcommunity.booking.internal.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.booking.api.BookingCancelledEvent;
import vn.edu.crs.smartcommunity.booking.api.BookingConfirmedEvent;
import vn.edu.crs.smartcommunity.booking.internal.dto.BookingResponse;
import vn.edu.crs.smartcommunity.booking.internal.dto.CreateBookingRequest;
import vn.edu.crs.smartcommunity.booking.internal.entity.Booking;
import vn.edu.crs.smartcommunity.booking.internal.entity.BookingStatus;
import vn.edu.crs.smartcommunity.booking.internal.repository.BookingRepository;
import vn.edu.crs.smartcommunity.common.error.ConflictException;
import vn.edu.crs.smartcommunity.common.error.BadRequestException;
import vn.edu.crs.smartcommunity.common.error.ForbiddenException;
import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.facility.api.FacilityInfo;
import vn.edu.crs.smartcommunity.facility.api.FacilityLookup;
import vn.edu.crs.smartcommunity.resident.api.ResidentInfo;
import vn.edu.crs.smartcommunity.resident.api.ResidentLookup;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResidentLookup residentLookup;
    private final FacilityLookup facilityLookup;
    private final ApplicationEventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository, ResidentLookup residentLookup,
            FacilityLookup facilityLookup, ApplicationEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.residentLookup = residentLookup;
        this.facilityLookup = facilityLookup;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookingResponse create(Long userId, CreateBookingRequest request) {
        ResidentInfo resident = residentLookup.getByUserId(userId).filter(ResidentInfo::active)
                .orElseThrow(() -> new NotFoundException("Active resident profile not found"));
        FacilityInfo facility = facilityLookup.getFacility(request.facilityId())
                .orElseThrow(() -> new NotFoundException("Facility not found"));
        validateFacility(facility);
        validateTimeWindow(facility, request.startTime(), request.endTime());
        if (bookingRepository.existsByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                facility.id(), BookingStatus.CONFIRMED, request.endTime(), request.startTime())) {
            throw new ConflictException("Facility is already booked for this time");
        }

        Booking booking = new Booking();
        booking.setCode(generateCode());
        booking.setResidentId(resident.residentId());
        booking.setFacilityId(facility.id());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.saveAndFlush(booking);
        eventPublisher.publishEvent(new BookingConfirmedEvent(saved.getId(), userId, facility.id(), facility.name(),
                saved.getStartTime(), saved.getEndTime()));
        return toResponse(saved, facility.name());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listMine(Long userId) {
        ResidentInfo resident = residentLookup.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Resident profile not found"));
        return bookingRepository.findByResidentIdOrderByStartTimeDesc(resident.residentId()).stream()
                .map(booking -> toResponse(booking, facilityName(booking.getFacilityId()))).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getMine(Long id, Long userId) {
        ResidentInfo resident = residentLookup.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Resident profile not found"));
        Booking booking = bookingRepository.findByIdAndResidentId(id, resident.residentId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        return toResponse(booking, facilityName(booking.getFacilityId()));
    }

    @Transactional
    public BookingResponse cancel(Long id, Long userId) {
        ResidentInfo resident = residentLookup.getByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Resident profile not found"));
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Booking not found"));
        if (!booking.getResidentId().equals(resident.residentId())) {
            throw new ForbiddenException("You cannot modify this booking");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed bookings can be cancelled");
        }
        if (!booking.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Only future bookings can be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(java.time.Instant.now());
        Booking saved = bookingRepository.saveAndFlush(booking);
        eventPublisher.publishEvent(new BookingCancelledEvent(saved.getId(), userId, saved.getFacilityId()));
        return toResponse(saved, facilityName(saved.getFacilityId()));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listManagement(BookingStatus status, Long facilityId) {
        List<Booking> bookings;
        if (status != null && facilityId != null) {
            bookings = bookingRepository.findByStatusAndFacilityIdOrderByStartTimeDesc(status, facilityId);
        } else if (status != null) {
            bookings = bookingRepository.findByStatusOrderByStartTimeDesc(status);
        } else if (facilityId != null) {
            bookings = bookingRepository.findByFacilityIdOrderByStartTimeDesc(facilityId);
        } else {
            bookings = bookingRepository.findAllByOrderByStartTimeDesc();
        }
        return bookings.stream().map(booking -> toResponse(booking, facilityName(booking.getFacilityId()))).toList();
    }

    private void validateFacility(FacilityInfo facility) {
        if (!facility.active() || !facility.bookable() || facility.status() != vn.edu.crs.smartcommunity.facility.api.FacilityStatus.AVAILABLE) {
            throw new ConflictException("Facility is not available for booking");
        }
    }

    private void validateTimeWindow(FacilityInfo facility, LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (!start.isBefore(end)) throw new BadRequestException("Booking start time must be before end time");
        if (!start.isAfter(now)) throw new BadRequestException("Booking must be in the future");
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new BadRequestException("Booking must start and end on the same day");
        }
        LocalTime opening = facility.openingTime();
        LocalTime closing = facility.closingTime();
        if (start.toLocalTime().isBefore(opening) || end.toLocalTime().isAfter(closing)) {
            throw new BadRequestException("Booking must be within facility opening hours");
        }
    }

    private String generateCode() {
        String code;
        do {
            code = "BKG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (bookingRepository.findByCode(code).isPresent());
        return code;
    }

    private String facilityName(Long facilityId) {
        return facilityLookup.getFacility(facilityId).map(FacilityInfo::name).orElse("Facility");
    }

    private BookingResponse toResponse(Booking booking, String facilityName) {
        return new BookingResponse(booking.getId(), booking.getCode(), booking.getResidentId(), booking.getFacilityId(),
                facilityName, booking.getStartTime(), booking.getEndTime(), booking.getStatus(), booking.getCreatedAt(),
                booking.getUpdatedAt(), booking.getCancelledAt());
    }
}

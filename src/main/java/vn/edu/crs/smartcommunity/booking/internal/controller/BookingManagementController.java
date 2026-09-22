package vn.edu.crs.smartcommunity.booking.internal.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.booking.internal.dto.BookingResponse;
import vn.edu.crs.smartcommunity.booking.internal.entity.BookingStatus;
import vn.edu.crs.smartcommunity.booking.internal.service.BookingService;

@RestController
@RequestMapping("/api/management/bookings")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class BookingManagementController {

    private final BookingService bookingService;

    public BookingManagementController(BookingService bookingService) { this.bookingService = bookingService; }

    @GetMapping
    public List<BookingResponse> list(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long facilityId) {
        return bookingService.listManagement(status, facilityId);
    }
}

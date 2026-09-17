package vn.edu.crs.smartcommunity.booking.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.crs.smartcommunity.booking.internal.dto.BookingResponse;
import vn.edu.crs.smartcommunity.booking.internal.dto.CreateBookingRequest;
import vn.edu.crs.smartcommunity.booking.internal.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("hasRole('RESIDENT')")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @PostMapping
    public BookingResponse create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(userId(jwt), request);
    }

    @GetMapping("/my")
    public List<BookingResponse> my(@AuthenticationPrincipal Jwt jwt) {
        return bookingService.listMine(userId(jwt));
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return bookingService.getMine(id, userId(jwt));
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return bookingService.cancel(id, userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        if (userId == null) throw new IllegalStateException("Authenticated token has no userId claim");
        return userId.longValue();
    }
}

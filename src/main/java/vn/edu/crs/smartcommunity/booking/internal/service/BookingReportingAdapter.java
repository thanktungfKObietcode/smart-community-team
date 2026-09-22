package vn.edu.crs.smartcommunity.booking.internal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.booking.api.BookingMetrics;
import vn.edu.crs.smartcommunity.booking.api.BookingReporting;
import vn.edu.crs.smartcommunity.booking.internal.entity.BookingStatus;
import vn.edu.crs.smartcommunity.booking.internal.repository.BookingRepository;

@Component
public class BookingReportingAdapter implements BookingReporting {

    private final BookingRepository repository;

    public BookingReportingAdapter(BookingRepository repository) { this.repository = repository; }

    @Override
    @Transactional(readOnly = true)
    public BookingMetrics getMetrics() {
        LocalDate today = LocalDate.now();
        var bookings = repository.findAll();
        return new BookingMetrics(bookings.stream().filter(b -> b.getStartTime().toLocalDate().equals(today)).count(),
                bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUpcomingByResidentId(Long residentId) {
        return repository.findByResidentIdOrderByStartTimeDesc(residentId).stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED && b.getStartTime().isAfter(LocalDateTime.now()))
                .count();
    }
}

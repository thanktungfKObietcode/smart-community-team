package vn.edu.crs.smartcommunity.booking.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.booking.internal.entity.Booking;
import vn.edu.crs.smartcommunity.booking.internal.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByResidentIdOrderByStartTimeDesc(Long residentId);

    Optional<Booking> findByIdAndResidentId(Long id, Long residentId);

    List<Booking> findAllByOrderByStartTimeDesc();

    List<Booking> findByStatusOrderByStartTimeDesc(BookingStatus status);

    List<Booking> findByFacilityIdOrderByStartTimeDesc(Long facilityId);

    Optional<Booking> findByCode(String code);

    List<Booking> findByStatusAndFacilityIdOrderByStartTimeDesc(BookingStatus status, Long facilityId);

    boolean existsByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long facilityId, BookingStatus status, java.time.LocalDateTime endTime,
            java.time.LocalDateTime startTime);
}

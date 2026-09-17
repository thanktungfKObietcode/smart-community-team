package vn.edu.crs.smartcommunity.property.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.property.internal.entity.Apartment;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findByBuildingIdAndActiveTrueOrderByUnitNumberAsc(Long buildingId);

    Optional<Apartment> findByIdAndActiveTrue(Long id);

    Optional<Apartment> findByBuildingIdAndUnitNumberIgnoreCase(Long buildingId, String unitNumber);

    Optional<Apartment> findByBuildingCodeIgnoreCaseAndUnitNumberIgnoreCase(
            String buildingCode,
            String unitNumber);

    boolean existsByBuildingIdAndUnitNumberIgnoreCase(Long buildingId, String unitNumber);
}

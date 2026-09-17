package vn.edu.crs.smartcommunity.facility.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.facility.internal.entity.Facility;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    List<Facility> findByActiveTrueOrderByCodeAsc();

    Optional<Facility> findByIdAndActiveTrue(Long id);

    Optional<Facility> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}

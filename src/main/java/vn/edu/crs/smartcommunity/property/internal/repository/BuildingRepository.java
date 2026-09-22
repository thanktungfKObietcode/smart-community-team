package vn.edu.crs.smartcommunity.property.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.property.internal.entity.Building;

public interface BuildingRepository extends JpaRepository<Building, Long> {

    List<Building> findByActiveTrueOrderByCodeAsc();

    Optional<Building> findByIdAndActiveTrue(Long id);

    Optional<Building> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}

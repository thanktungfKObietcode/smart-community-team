package vn.edu.crs.smartcommunity.resident.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.resident.internal.entity.Resident;

public interface ResidentRepository extends JpaRepository<Resident, Long> {

    Optional<Resident> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    long countByActiveTrue();

    List<Resident> findAllByOrderByIdAsc();
}

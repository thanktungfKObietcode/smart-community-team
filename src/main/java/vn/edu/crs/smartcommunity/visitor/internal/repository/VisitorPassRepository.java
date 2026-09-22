package vn.edu.crs.smartcommunity.visitor.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPass;

public interface VisitorPassRepository extends JpaRepository<VisitorPass, Long> {

    List<VisitorPass> findByResidentIdOrderByValidFromDesc(Long residentId);

    Optional<VisitorPass> findByIdAndResidentId(Long id, Long residentId);

    Optional<VisitorPass> findByCodeIgnoreCase(String code);
}

package vn.edu.crs.smartcommunity.visitor.internal.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.visitor.api.VisitorMetrics;
import vn.edu.crs.smartcommunity.visitor.api.VisitorReporting;
import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPassStatus;
import vn.edu.crs.smartcommunity.visitor.internal.repository.VisitorPassRepository;

@Component
public class VisitorReportingAdapter implements VisitorReporting {

    private final VisitorPassRepository repository;

    public VisitorReportingAdapter(VisitorPassRepository repository) { this.repository = repository; }

    @Override
    @Transactional(readOnly = true)
    public VisitorMetrics getMetrics() {
        LocalDateTime now = LocalDateTime.now();
        var passes = repository.findAll();
        return new VisitorMetrics(passes.stream().filter(p -> !p.getValidFrom().isAfter(now)
                && !p.getValidUntil().isBefore(now)
                && (p.getStatus() == VisitorPassStatus.ACTIVE || p.getStatus() == VisitorPassStatus.CHECKED_IN)).count(),
                passes.stream().filter(p -> p.getStatus() == VisitorPassStatus.CHECKED_IN).count());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByResidentId(Long residentId) {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByResidentIdOrderByValidFromDesc(residentId).stream()
                .filter(p -> p.getStatus() == VisitorPassStatus.ACTIVE && p.getValidUntil().isAfter(now)).count();
    }
}

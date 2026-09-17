package vn.edu.crs.smartcommunity.visitor.internal.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPass;
import vn.edu.crs.smartcommunity.visitor.internal.entity.VisitorPassStatus;
import vn.edu.crs.smartcommunity.visitor.internal.repository.VisitorPassRepository;

@Service
public class VisitorPassExpirationService {

    private final VisitorPassRepository visitorPassRepository;

    public VisitorPassExpirationService(VisitorPassRepository visitorPassRepository) {
        this.visitorPassRepository = visitorPassRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean expireIfNecessary(Long passId) {
        VisitorPass pass = visitorPassRepository.findById(passId).orElse(null);
        if (pass == null || pass.getStatus() != VisitorPassStatus.ACTIVE
                || !LocalDateTime.now().isAfter(pass.getValidUntil())) {
            return false;
        }
        pass.setStatus(VisitorPassStatus.EXPIRED);
        visitorPassRepository.saveAndFlush(pass);
        return true;
    }
}

package vn.edu.crs.smartcommunity.resident.internal.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.resident.api.ResidentInfo;
import vn.edu.crs.smartcommunity.resident.api.ResidentLookup;
import vn.edu.crs.smartcommunity.resident.internal.entity.Resident;
import vn.edu.crs.smartcommunity.resident.internal.repository.ResidentRepository;

@Service
public class ResidentLookupAdapter implements ResidentLookup {

    private final ResidentRepository residentRepository;

    public ResidentLookupAdapter(ResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResidentInfo> getById(Long residentId) {
        return residentRepository.findById(residentId).map(this::toInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResidentInfo> getByUserId(Long userId) {
        return residentRepository.findByUserId(userId).map(this::toInfo);
    }

    private ResidentInfo toInfo(Resident resident) {
        return new ResidentInfo(
                resident.getId(),
                resident.getUserId(),
                resident.getApartmentId(),
                resident.isActive());
    }

    @Override
    public long countActive() {
        return residentRepository.countByActiveTrue();
    }
}

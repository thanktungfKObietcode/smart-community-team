package vn.edu.crs.smartcommunity.resident.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.common.error.ConflictException;
import vn.edu.crs.smartcommunity.common.error.NotFoundException;
import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.identity.api.IdentityUserInfo;
import vn.edu.crs.smartcommunity.property.api.ApartmentInfo;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;
import vn.edu.crs.smartcommunity.resident.internal.dto.CreateResidentRequest;
import vn.edu.crs.smartcommunity.resident.internal.dto.ResidentResponse;
import vn.edu.crs.smartcommunity.resident.internal.entity.Resident;
import vn.edu.crs.smartcommunity.resident.internal.repository.ResidentRepository;

@Service
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final IdentityLookup identityLookup;
    private final PropertyLookup propertyLookup;

    public ResidentService(
            ResidentRepository residentRepository,
            IdentityLookup identityLookup,
            PropertyLookup propertyLookup) {
        this.residentRepository = residentRepository;
        this.identityLookup = identityLookup;
        this.propertyLookup = propertyLookup;
    }

    @Transactional
    public ResidentResponse createResident(CreateResidentRequest request) {
        if (residentRepository.existsByUserId(request.userId())) {
            throw new ConflictException("Resident profile already exists");
        }

        IdentityUserInfo user = identityLookup.getUserById(request.userId())
                .orElseThrow(() -> new NotFoundException("Identity user not found"));
        validateUser(user);

        ApartmentInfo apartment = propertyLookup.getApartment(request.apartmentId())
                .orElseThrow(() -> new NotFoundException("Apartment not found or inactive"));

        Resident resident = new Resident();
        resident.setUserId(user.userId());
        resident.setApartmentId(apartment.id());
        resident.setResidentType(request.residentType());
        resident.setPhone(normalizePhone(request.phone()));
        resident.setActive(true);
        resident.setMoveInDate(request.moveInDate());
        return toResponse(residentRepository.saveAndFlush(resident), user, apartment);
    }

    @Transactional(readOnly = true)
    public ResidentResponse getMyProfile(Long userId) {
        Resident resident = residentRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Resident profile not found"));
        return toResponse(resident);
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> listResidents() {
        return residentRepository.findAllByOrderByIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateUser(IdentityUserInfo user) {
        if (!user.active()) {
            throw new ConflictException("Identity user is inactive");
        }
        if (!user.roles().contains("RESIDENT")) {
            throw new ConflictException("Identity user must have RESIDENT role");
        }
    }

    private ResidentResponse toResponse(Resident resident) {
        IdentityUserInfo user = identityLookup.getUserById(resident.getUserId())
                .orElseThrow(() -> new NotFoundException("Identity user not found"));
        ApartmentInfo apartment = propertyLookup.getApartment(resident.getApartmentId())
                .orElseThrow(() -> new NotFoundException("Apartment not found or inactive"));
        return toResponse(resident, user, apartment);
    }

    private ResidentResponse toResponse(
            Resident resident,
            IdentityUserInfo user,
            ApartmentInfo apartment) {
        return new ResidentResponse(
                resident.getId(),
                resident.getUserId(),
                user.fullName(),
                user.email(),
                resident.getPhone(),
                resident.getResidentType(),
                resident.isActive(),
                resident.getMoveInDate(),
                apartment);
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}

package vn.edu.crs.smartcommunity.facility.internal.config;

import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import vn.edu.crs.smartcommunity.facility.internal.entity.Facility;
import vn.edu.crs.smartcommunity.facility.api.FacilityStatus;
import vn.edu.crs.smartcommunity.facility.api.FacilityType;
import vn.edu.crs.smartcommunity.facility.internal.repository.FacilityRepository;
import vn.edu.crs.smartcommunity.property.api.BuildingInfo;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;

@Component
@Order(300)
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class FacilityDemoDataInitializer implements CommandLineRunner {

    private final FacilityRepository facilityRepository;
    private final PropertyLookup propertyLookup;

    public FacilityDemoDataInitializer(FacilityRepository facilityRepository, PropertyLookup propertyLookup) {
        this.facilityRepository = facilityRepository;
        this.propertyLookup = propertyLookup;
    }

    @Override
    public void run(String... args) {
        BuildingInfo building = propertyLookup.getBuildingByCode("A2").orElse(null);
        if (building == null) return;
        createIfMissing("BADMINTON-A2", "Sân cầu lông A2", FacilityType.BADMINTON_COURT,
                building.id(), LocalTime.of(6, 0), LocalTime.of(22, 0));
        createIfMissing("COMMUNITY-A2", "Phòng sinh hoạt cộng đồng A2", FacilityType.COMMUNITY_ROOM,
                building.id(), LocalTime.of(8, 0), LocalTime.of(21, 0));
    }

    private void createIfMissing(String code, String name, FacilityType type, Long buildingId,
            LocalTime opening, LocalTime closing) {
        if (facilityRepository.findByCodeIgnoreCase(code).isPresent()) return;
        Facility facility = new Facility();
        facility.setCode(code);
        facility.setName(name);
        facility.setBuildingId(buildingId);
        facility.setType(type);
        facility.setStatus(FacilityStatus.AVAILABLE);
        facility.setBookable(true);
        facility.setOpeningTime(opening);
        facility.setClosingTime(closing);
        facility.setActive(true);
        facilityRepository.saveAndFlush(facility);
    }
}

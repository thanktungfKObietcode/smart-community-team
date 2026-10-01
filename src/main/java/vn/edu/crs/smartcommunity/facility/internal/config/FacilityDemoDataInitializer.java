package vn.edu.crs.smartcommunity.facility.internal.config;

import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import vn.edu.crs.smartcommunity.facility.api.FacilityStatus;
import vn.edu.crs.smartcommunity.facility.api.FacilityType;
import vn.edu.crs.smartcommunity.facility.internal.entity.Facility;
import vn.edu.crs.smartcommunity.facility.internal.repository.FacilityRepository;
import vn.edu.crs.smartcommunity.property.api.BuildingInfo;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;

/** Small, idempotent catalogue used only when the explicit demo flag is enabled. */
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
        createIfMissing("BADMINTON-A2", "Sân cầu lông A2", "Tầng 1, tòa A2", "Sân trong nhà cho cư dân đặt lịch theo giờ.",
                FacilityType.BADMINTON_COURT, building.id(), 8, "/facilities/badminton.svg", LocalTime.of(6, 0), LocalTime.of(22, 0));
        createIfMissing("COMMUNITY-A2", "Phòng sinh hoạt cộng đồng A2", "Tầng 1, tòa A2", "Không gian tổ chức hoạt động cộng đồng và sinh hoạt chung.",
                FacilityType.COMMUNITY_ROOM, building.id(), 40, "/facilities/community.svg", LocalTime.of(8, 0), LocalTime.of(21, 0));
        createIfMissing("GYM-A2", "Phòng Gym A2", "Tầng 2, tòa A2", "Khu luyện tập với thiết bị cơ bản dành cho cư dân.",
                FacilityType.GYM, building.id(), 20, "/facilities/gym.svg", LocalTime.of(5, 30), LocalTime.of(22, 0));
        createIfMissing("READING-A2", "Phòng đọc sách A2", "Tầng 2, tòa A2", "Không gian yên tĩnh để đọc sách và học tập.",
                FacilityType.READING_ROOM, building.id(), 18, "/facilities/reading.svg", LocalTime.of(8, 0), LocalTime.of(21, 0));
        createIfMissing("MEETING-A2", "Phòng họp A2", "Tầng 2, tòa A2", "Phòng họp nhỏ cho các hoạt động của cư dân.",
                FacilityType.MEETING_ROOM, building.id(), 16, "/facilities/meeting.svg", LocalTime.of(8, 0), LocalTime.of(21, 0));
        createIfMissing("BBQ-A2", "Khu BBQ A2", "Sân vườn, tòa A2", "Khu nướng ngoài trời cho các buổi gặp gỡ cuối tuần.",
                FacilityType.OTHER, building.id(), 24, "/facilities/community.svg", LocalTime.of(9, 0), LocalTime.of(21, 0));
    }

    private void createIfMissing(String code, String name, String location, String description, FacilityType type,
            Long buildingId, int capacity, String coverImageUrl, LocalTime opening, LocalTime closing) {
        if (facilityRepository.findByCodeIgnoreCase(code).isPresent()) return;
        Facility facility = new Facility();
        facility.setCode(code);
        facility.setName(name);
        facility.setLocation(location);
        facility.setDescription(description);
        facility.setBuildingId(buildingId);
        facility.setCapacity(capacity);
        facility.setCoverImageUrl(coverImageUrl);
        facility.setGalleryImageUrls(List.of(coverImageUrl));
        facility.setType(type);
        facility.setStatus(FacilityStatus.AVAILABLE);
        facility.setBookable(true);
        facility.setOpeningTime(opening);
        facility.setClosingTime(closing);
        facility.setActive(true);
        facilityRepository.saveAndFlush(facility);
    }
}

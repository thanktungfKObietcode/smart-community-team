package vn.edu.crs.smartcommunity.property.internal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import vn.edu.crs.smartcommunity.property.internal.entity.Apartment;
import vn.edu.crs.smartcommunity.property.internal.entity.Building;
import vn.edu.crs.smartcommunity.property.internal.repository.ApartmentRepository;
import vn.edu.crs.smartcommunity.property.internal.repository.BuildingRepository;

@Component
@Order(100)
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class PropertyDemoDataInitializer implements CommandLineRunner {

    private final BuildingRepository buildingRepository;
    private final ApartmentRepository apartmentRepository;

    public PropertyDemoDataInitializer(
            BuildingRepository buildingRepository,
            ApartmentRepository apartmentRepository) {
        this.buildingRepository = buildingRepository;
        this.apartmentRepository = apartmentRepository;
    }

    @Override
    public void run(String... args) {
        Building building = buildingRepository.findByCodeIgnoreCase("A2")
                .orElseGet(() -> createBuilding());
        if (building.getId() == null) {
            building = buildingRepository.saveAndFlush(building);
        }

        if (apartmentRepository.findByBuildingIdAndUnitNumberIgnoreCase(building.getId(), "A1205")
                .isEmpty()) {
            Apartment apartment = new Apartment();
            apartment.setBuilding(building);
            apartment.setUnitNumber("A1205");
            apartment.setFloorNumber(12);
            apartment.setActive(true);
            apartmentRepository.saveAndFlush(apartment);
        }
    }

    private Building createBuilding() {
        Building building = new Building();
        building.setCode("A2");
        building.setName("Tòa A2");
        building.setAddress("Green City Residence");
        building.setActive(true);
        return building;
    }
}

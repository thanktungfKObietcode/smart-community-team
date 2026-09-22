package vn.edu.crs.smartcommunity.resident.internal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;
import vn.edu.crs.smartcommunity.identity.api.IdentityUserInfo;
import vn.edu.crs.smartcommunity.property.api.ApartmentInfo;
import vn.edu.crs.smartcommunity.property.api.PropertyLookup;
import vn.edu.crs.smartcommunity.resident.internal.entity.Resident;
import vn.edu.crs.smartcommunity.resident.internal.entity.ResidentType;
import vn.edu.crs.smartcommunity.resident.internal.repository.ResidentRepository;

@Component
@Order(200)
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class ResidentDemoDataInitializer implements CommandLineRunner {

    private final ResidentRepository residentRepository;
    private final IdentityLookup identityLookup;
    private final PropertyLookup propertyLookup;

    public ResidentDemoDataInitializer(
            ResidentRepository residentRepository,
            IdentityLookup identityLookup,
            PropertyLookup propertyLookup) {
        this.residentRepository = residentRepository;
        this.identityLookup = identityLookup;
        this.propertyLookup = propertyLookup;
    }

    @Override
    public void run(String... args) {
        IdentityUserInfo user = identityLookup.getUserByEmail("resident@test.com").orElse(null);
        if (user == null || !user.active() || !user.roles().contains("RESIDENT")) {
            return;
        }

        if (residentRepository.existsByUserId(user.userId())) {
            return;
        }

        ApartmentInfo apartment = propertyLookup.getApartment("A2", "A1205").orElse(null);
        if (apartment == null) {
            return;
        }

        Resident resident = new Resident();
        resident.setUserId(user.userId());
        resident.setApartmentId(apartment.id());
        resident.setResidentType(ResidentType.OWNER);
        resident.setActive(true);
        residentRepository.saveAndFlush(resident);
    }
}

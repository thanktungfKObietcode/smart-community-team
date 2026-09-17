package vn.edu.crs.smartcommunity;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureVerificationTests {

    @Test
    void applicationModulesHaveNoCyclesOrIllegalDependencies() {
        ApplicationModules.of(SmartCommunityApplication.class).verify();
    }
}

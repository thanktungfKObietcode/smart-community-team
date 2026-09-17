package vn.edu.crs.smartcommunity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import vn.edu.crs.smartcommunity.testsupport.TestDatabaseSafetyConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDatabaseSafetyConfiguration.class)
class SmartCommunityApplicationTests {

    @Test
    void contextLoads() {
    }

}

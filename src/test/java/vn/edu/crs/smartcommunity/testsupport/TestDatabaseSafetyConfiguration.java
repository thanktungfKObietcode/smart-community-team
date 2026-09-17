package vn.edu.crs.smartcommunity.testsupport;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@TestConfiguration(proxyBeanMethods = false)
public class TestDatabaseSafetyConfiguration {

    private static final String REQUIRED_DATABASE = "smartcommunity_test";

    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner testDatabaseGuard(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                String database = connection.getCatalog();
                String url = connection.getMetaData().getURL();
                if (!REQUIRED_DATABASE.equalsIgnoreCase(database)
                        || url == null || !url.matches(".*[/]" + REQUIRED_DATABASE + "(?:[?].*)?$")) {
                    throw new IllegalStateException(
                            "Automated tests must use database '" + REQUIRED_DATABASE + "' but resolved "
                                    + database + " from " + url);
                }
            }
        };
    }
}

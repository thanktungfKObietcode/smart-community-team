package vn.edu.crs.smartcommunity.notification.internal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate's enum check constraint is not widened by ddl-auto=update after new
 * enum values are introduced. This additive compatibility step preserves existing
 * notifications while allowing the current notification event types.
 */
@Component
@Order(250)
public class NotificationSchemaCompatibilityInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public NotificationSchemaCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check");
        jdbcTemplate.execute("ALTER TABLE notifications ADD CONSTRAINT notifications_type_check "
                + "CHECK (type IN ('SERVICE_REQUEST_ASSIGNED', 'SERVICE_REQUEST_RESOLVED', "
                + "'BOOKING_CONFIRMED', 'VISITOR_CHECKED_IN'))");
    }
}

package vn.edu.crs.smartcommunity.servicerequest.internal.config;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Temporary development compatibility for enum check constraints; replace with Flyway before production. */
@Component
public class ServiceRequestSchemaCompatibilityInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ServiceRequestSchemaCompatibilityInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE IF EXISTS service_requests ADD COLUMN IF NOT EXISTS code VARCHAR(20)");
        // Older development rows may use the pre-canonical priority. Preserve them while normalizing the value.
        jdbcTemplate.update("UPDATE service_requests SET priority = 'CRITICAL' WHERE priority = 'URGENT'");
        jdbcTemplate.update("UPDATE service_requests SET code = 'REQ-' || LPAD(id::text, 8, '0') "
                + "WHERE code IS NULL OR BTRIM(code) = ''");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_service_requests_code ON service_requests(code)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS service_requests ALTER COLUMN code SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS service_requests DROP CONSTRAINT IF EXISTS service_requests_priority_check");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS service_requests ADD CONSTRAINT service_requests_priority_check "
                + "CHECK (priority IN ('LOW','NORMAL','HIGH','CRITICAL'))");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS service_requests DROP CONSTRAINT IF EXISTS service_requests_status_check");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS service_requests ADD CONSTRAINT service_requests_status_check "
                + "CHECK (status IN ('OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','CANCELLED'))");
    }
}

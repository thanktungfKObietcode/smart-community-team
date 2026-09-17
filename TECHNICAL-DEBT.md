# Technical debt

- `spring.jpa.hibernate.ddl-auto=update` is for development only.
- `NotificationSchemaCompatibilityInitializer` is a temporary compatibility workaround.
- `ServiceRequestSchemaCompatibilityInitializer` is temporary for the ServiceRequest code column backfill and canonical priority constraint.
- Replace runtime schema compatibility changes with Flyway migrations before production deployment.
- The automated test profile uses a disposable `create-drop` schema in `smartcommunity_test`; the development database remains non-destructive (`ddl-auto=update`).
- `scripts/reset-dev-data.sql` is an explicit, guarded development-data reset for manual demos and must not be used as application startup logic.

-- DEVELOPMENT-ONLY DATA RESET
-- This removes application data while preserving the database schema.
-- Do not run automatically at application startup.

DO $$
BEGIN
    IF current_database() <> 'smartcommunity' THEN
        RAISE EXCEPTION 'Refusing to reset database %. This script is only for smartcommunity.', current_database();
    END IF;
END
$$;

TRUNCATE TABLE
    event_publication,
    audit_logs,
    notifications,
    visitor_passes,
    bookings,
    service_requests,
    residents,
    facilities,
    apartments,
    buildings,
    user_roles,
    users,
    roles
RESTART IDENTITY CASCADE;

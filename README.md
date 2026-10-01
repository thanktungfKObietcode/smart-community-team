# Smart Community

Smart Community is a Spring Boot modular-monolith backend with a Vue 3 + Vite + PrimeVue web client.

## Run the backend

From the project root on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs at `http://localhost:8080`.

## Run the frontend

Open a second terminal from the project root:

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs at `http://localhost:5173`.

Vite proxies all `/api` requests to `http://localhost:8080`, so the browser client uses relative URLs such as `/api/auth/login` and does not need development CORS configuration.

## Database separation

The development/manual-testing database is `smartcommunity`. The automated test database is `smartcommunity_test`.

- Browser/manual testing uses the backend connected to `smartcommunity`.
- `.\mvnw.cmd test` uses the `test` profile and connects only to `smartcommunity_test` with a disposable `create-drop` schema.
- The test suite has a fail-fast database guard and must never be pointed at `smartcommunity`.

To recreate the clean manual-testing baseline, stop the backend, verify a current `pg_dump` backup exists, and run the guarded script against the development database:

```powershell
psql -h localhost -U postgres -d smartcommunity -f scripts/reset-dev-data.sql
.\mvnw.cmd spring-boot:run
```

The script refuses to run against any database other than `smartcommunity`, preserves the schema, and removes application data. It is intentionally never run during normal startup.

## Account lifecycle and demo mode

By default, `app.demo-data.enabled=false`; normal startup does not create demo Manager, Resident, Technician, or Security accounts. It only ensures the configured bootstrap ADMIN exists. Override bootstrap values outside development with:

```powershell
$env:APP_BOOTSTRAP_ADMIN_EMAIL = 'admin@your-community.example'
$env:APP_BOOTSTRAP_ADMIN_INITIAL_PASSWORD = 'a-strong-initial-password'
```

The bootstrap initializer is idempotent and never resets an existing password. The fallback credentials in `application.properties` are development-only and must not be used for deployment.

To enable the small local demonstration dataset, set the feature flag before starting the backend:

```powershell
$env:APP_DEMO_DATA_ENABLED = 'true'
.\mvnw.cmd spring-boot:run
```

Set `VITE_SHOW_DEMO_ACCOUNTS=true` in `frontend/.env.local` only when the frontend should display the demo-account hint. See `frontend/.env.example`.

When demo mode is enabled, these accounts use password `123456`:

All development demo accounts use password `123456`:

| Role | Email | Portal |
| --- | --- | --- |
| Resident | `resident@test.com` | `/resident/dashboard` |
| Manager | `manager@test.com` | `/manager/dashboard` |
| Technician | `technician@test.com` | `/technician/dashboard` |
| Security | `security@test.com` | `/security/visitors` |
| Administrator | `admin@test.com` | `/manager/dashboard` |

Start at `http://localhost:5173/login`; the app redirects each role to its portal after successful authentication.

In normal operation, ADMIN creates MANAGER, TECHNICIAN, and SECURITY accounts at `/manager/accounts`. ADMIN or MANAGER adds a Resident from `/manager/residents`; that workflow creates the RESIDENT identity account and Resident profile atomically after an apartment is selected.

## Final architecture and demo flow

Vue 3 frontend → REST API → Spring Boot Modular Monolith → PostgreSQL.

Main role routes:

- Resident: `/resident/dashboard`
- Manager/Admin: `/manager/dashboard`
- Technician: `/technician/dashboard`
- Security: `/security/visitors`

Recommended presentation flow:

1. Resident reports an issue.
2. Manager assigns an active technician by name.
3. Technician starts and resolves the request.
4. Resident confirms completion and receives notifications.
5. Resident books an available facility, then creates a visitor pass.
6. Security looks up the pass and checks the visitor in and out.

Facility booking demo: use the resident **Đặt tiện ích** page, choose an available facility, and submit a future time within its opening hours. Visitor demo: use **Khách thăm** to create a pass, then enter its generated code in the Security portal.

Service requests use server-generated immutable `REQ-XXXXXXXX` business codes. Legacy `URGENT` priorities are normalized to `CRITICAL` by the development compatibility initializer; the current database contains no `URGENT` rows.

## Authentication note

For this course MVP the JWT and minimal current-user data are stored in `sessionStorage`. Production hardening should use a safer strategy, such as secure HttpOnly cookies with a Backend for Frontend (BFF), along with appropriate CSRF protections.

# SVIR ERP

Non-profit ERP system for the SVIR organization — Spring Boot 3 REST API backed by MySQL 8 with Flyway schema migrations.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Quick Start](#quick-start)
4. [Configuration](#configuration)
5. [Authentication](#authentication)
6. [Admin Settings](#admin-settings)
7. [Building](#building)
8. [Database Migrations](#database-migrations)
9. [Running the API](#running-the-api)
10. [Production Access](#production-access)
11. [Angular UI](#angular-ui)
12. [API Overview](#api-overview)
13. [Migration Reference](#migration-reference)

---

## Prerequisites

| Requirement | Minimum | Notes |
|---|---|---|
| Java JDK | 25 | `java -version` to confirm |
| MySQL | 8.0 | Must be running before starting the app |
| Maven | — | **Not required** — use the included wrapper (`mvnw`) |
| Node.js | 20 LTS | Only needed for the Angular UI |
| Google Cloud / Workspace admin access | — | Needed once to create an OAuth 2.0 Client — see [Authentication](#authentication). Unlike MySQL credentials, the app starts fine without one configured; Google sign-in just won't work until it's set on the admin [Settings page](#admin-settings). |

---

## Project Structure

```
svirerp/
├── src/
│   └── main/
│       ├── java/com/svivanrilski/svirerp/
│       │   ├── common/          # GlobalExceptionHandler, ResourceNotFoundException
│       │   ├── person/          # Entity, Repository, Service, Controller
│       │   ├── organization/
│       │   ├── membership/
│       │   ├── governance/
│       │   ├── event/
│       │   ├── volunteer/
│       │   ├── finance/
│       │   ├── zeffyimport/    # Zeffy payment import (.xlsx/.csv) — spans membership + finance
│       │   ├── settings/       # Admin-only app_setting key/value store (Google OAuth creds, etc.)
│       │   └── email/          # Gmail API email sending + Connect Gmail OAuth flow
│       └── resources/
│           ├── application.properties              # Base config (env-var placeholders)
│           ├── application-local.properties.example  # Copy & fill for local dev
│           └── db/migration/                       # Flyway V1–V41 SQL scripts
├── ui/                          # Angular 21 front-end (see Angular UI section)
├── mvnw                         # Unix Maven Wrapper
├── mvnw.cmd                     # Windows Maven Wrapper
└── pom.xml
```

---

## Quick Start

```bash
# 1. Create the MySQL database (once)
mysql -u root -p -e "CREATE DATABASE svirerp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Configure credentials (see Configuration section)
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
# Edit application-local.properties with your MySQL user/password

# 3. Run Flyway migrations (creates all 31 tables)
./mvnw flyway:migrate \
  -Dflyway.url="jdbc:mysql://localhost:3306/svirerp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" \
  -Dflyway.user=root \
  -Dflyway.password=your_password

# 4. Set up Google sign-in (see Authentication section): enable the
#    break-glass local admin login via env vars/application-local.properties,
#    log in at /portal-access, then enter your dev OAuth Client's id/secret
#    on the admin Settings page.

# 5. Build and run
./mvnw clean package -DskipTests
java -jar target/svirerp-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# API is now available at http://localhost:8080/api
```

> **Windows CMD / PowerShell:** replace `./mvnw` with `mvnw.cmd` in every command below.

---

## Configuration

### Environment variables (production / CI)

The preferred way to supply credentials in any non-local environment is via environment variables:

| Variable | Description | Default (local) |
|---|---|---|
| `SVIRERP_DB_URL` | Full JDBC URL | `jdbc:mysql://localhost:3306/svirerp?...` |
| `SVIRERP_DB_USER` | Database username | `root` |
| `SVIRERP_DB_PASSWORD` | Database password | *(empty)* |

```bash
export SVIRERP_DB_URL="jdbc:mysql://db-host:3306/svirerp?useSSL=true&serverTimezone=UTC"
export SVIRERP_DB_USER=svirerp
export SVIRERP_DB_PASSWORD=strong-secret
java -jar target/svirerp-1.0.0-SNAPSHOT.jar
```

### Local developer override

For local development, credentials can be placed in a gitignored file instead of exporting environment variables:

```bash
# 1. Copy the example
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties

# 2. Edit application-local.properties — fill in your MySQL user/password
#    (this file is listed in .gitignore and will never be committed)

# 3. Activate the local profile when starting the app
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# or
java -jar target/svirerp-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

### Command-line override (one-off)

Any property can be overridden on the command line without touching any file:

```bash
java -jar target/svirerp-1.0.0-SNAPSHOT.jar \
  --spring.datasource.username=root \
  --spring.datasource.password=secret \
  --server.port=9090
```

### Key properties

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `spring.jpa.show-sql` | `false` | Log SQL to console |
| `spring.flyway.baseline-on-migrate` | `true` | Safe for databases that already have data |
| `app.cors.allowed-origins` | `http://localhost:4200` | Comma-separated origins allowed to call `/api/**`; override via `SVIRERP_CORS_ALLOWED_ORIGINS` when the UI is served from a different host |
| `app.auth.google.allowed-domain` | `svivanrilski.com` | Google Workspace hosted domain allowed to sign in; override via `SVIRERP_GOOGLE_ALLOWED_DOMAIN` |
| `app.auth.admin.username` / `app.auth.admin.password-hash` | *(empty — disabled)* | Break-glass local admin login; blank disables it. Set via `SVIRERP_ADMIN_USERNAME` / `SVIRERP_ADMIN_PASSWORD_HASH` |
| `app.settings.encryption-key` / `app.settings.encryption-salt` | *(empty — required for SECRET settings)* | Encrypts `SECRET`-type rows in the `app_setting` table (e.g. the Google OAuth client secret). Salt must be hex-encoded. Set via `SVIRERP_SETTINGS_ENCRYPTION_KEY` / `SVIRERP_SETTINGS_ENCRYPTION_SALT` — see [Admin Settings](#admin-settings) |

> **Note:** The Google OAuth 2.0 client ID/secret are **not** properties — they're configured at runtime via the admin-only Settings page and stored (secret encrypted) in the `app_setting` table, so they can be rotated with no restart. See [Admin Settings](#admin-settings).

---

## Authentication

The API is gated behind a login — every `/api/**` request requires an authenticated session. Login is purely a gate: once signed in, via either method below, a user has the same full access the API already exposes. There are two ways in:

### Google sign-in (primary)

Restricted to accounts in the org's Google Workspace domain (`app.auth.google.allowed-domain`, default `svivanrilski.com`), enforced twice — once by Google itself (consent screen set to "Internal") and again by the app (checking the `hd` claim on the ID token).

**One-time Google Cloud Console setup:**

1. **OAuth consent screen** → set **User Type = Internal**. This restricts sign-in to your Workspace org at Google's level, before the app ever sees the request.
2. **Create two OAuth 2.0 Client IDs** (Web application type) — one per environment, so dev and prod secrets are independent:
   - **Dev**: Authorized redirect URI `http://localhost:8080/login/oauth2/code/google`
   - **Prod**: Authorized redirect URI `https://svirerp.svivanrilski.com/login/oauth2/code/google`
3. Log in as the [local admin](#local-admin-login-break-glass-fallback) and enter the **dev** client's id/secret on the admin-only Settings page (`/settings`). For prod, log in as the prod deployment's local admin and do the same there — each environment's credentials live only in its own database, never in a properties file. See [Admin Settings](#admin-settings) for details.

Unlike MySQL credentials, a blank Google client ID does **not** prevent the app from starting — it just means the `google` OAuth2 client registration resolves to nothing, so Google sign-in fails (`/oauth2/authorization/google` 404s) until it's configured on the Settings page. This is also why the local admin login must be set up first: it's the only way to reach the Settings page and bootstrap Google sign-in.

### Local admin login (break-glass fallback)

A single admin account, for when Google sign-in is unavailable. Disabled by default (both properties blank). To enable it:

1. Generate a bcrypt hash of your chosen password — there's no script for this in the repo; the simplest way is a throwaway call to `new BCryptPasswordEncoder().encode("your-password")` (e.g. in a scratch test or a `jshell` session with Spring Security on the classpath).
2. Set `app.auth.admin.username` and `app.auth.admin.password-hash` (the bcrypt hash, never the plaintext password) via `SVIRERP_ADMIN_USERNAME` / `SVIRERP_ADMIN_PASSWORD_HASH` in prod, or in `application-local.properties` for local testing.
3. Sign in at `/portal-access` — this route is intentionally not linked from the login page or any navigation; it's meant to be known only to whoever holds the admin credentials.

Failed attempts are rate-limited (5 failures per IP within 15 minutes triggers a 15-minute lockout) and every attempt — success, failure, or lockout — is audit-logged under the `AUDIT.local-admin-login` logger name.

---

## Admin Settings

An admin-only Settings area (`/settings` in the UI, gated by `ROLE_ADMIN` — only the [local admin login](#local-admin-login-break-glass-fallback) has this role, so Google-authenticated users never see it) covers the following:

### General settings

Generic key/value configuration backed by the `app_setting` table (migration V28) — new settings are a migration row away rather than new plumbing. Each row has a `valueType`; `SECRET`-type values (currently `google.oauth.client-id` and `google.oauth.client-secret`) are encrypted at rest with `app.settings.encryption-key`/`app.settings.encryption-salt` and are **never** sent back to the frontend, even as ciphertext — the API only reports a `hasValue` flag. Changing the Google client id/secret here takes effect on the very next login attempt, with no app restart, since `SecurityConfig` resolves them fresh from the table on every OAuth2 login (see [Authentication](#authentication)).

- `GET /api/settings` — list all settings (secrets redacted)
- `PUT /api/settings/{key}` — update a single setting's value

### Organization

This is a single-org-per-installation app — there's no organization list/picker. The Organization tab under Settings (`/settings/organization`) is the one place to view/edit that org's own details; it replaced the standalone Organizations list/form pages and nav item.

### Gmail (email sending)

The Gmail tab under Settings (`/settings/gmail`, migration V31) lets an admin connect a Gmail account so the app can send email through the **Gmail API** (not SMTP) using an OAuth2 refresh token — no app password or SMTP credentials ever touch the server.

**One-time Google Cloud Console setup** (separate OAuth Client from the one used for [Google sign-in](#authentication)):

1. Create an OAuth 2.0 Client ID (Web application type) with authorized redirect URI `{origin}/api/settings/gmail/callback` (e.g. `http://localhost:8080/api/settings/gmail/callback` for local dev) and both the `gmail.send` and `userinfo.email` scopes — the latter is required because the callback also calls Google's userinfo endpoint to record the connected account's address (`gmail.sender-address`); a token minted with `gmail.send` alone gets a 401 from that call.
2. On the admin Settings → Gmail page, paste the client ID and secret and save.
3. Click **Connect Gmail** — this starts the OAuth consent flow (`GET /api/settings/gmail/authorize-url`) and, on approval, the callback (`GET /api/settings/gmail/callback`) exchanges the auth code for a refresh token and stores it (encrypted) along with the connected account's address (`gmail.sender-address`) in `app_setting`.
4. Use **Send Test Email** on the same page to confirm the connection works end-to-end (subject to the [Email switch](#email-central-onofftest-switch) below — it's `DISABLED` by default, so connect that first if the test send is unexpectedly blocked).

Access tokens are minted on demand from the stored refresh token (`GmailTokenService`, cached in memory until near expiry) — rotating the client secret or reconnecting the account takes effect immediately, no restart needed, same pattern as Google sign-in's client credentials.

- `GET /api/settings/gmail/authorize-url` — builds the Google consent URL (`ROLE_ADMIN`)
- `GET /api/settings/gmail/callback` — OAuth2 redirect target; not called directly
- `POST /api/settings/gmail/test-send` — `{ "to": "..." }`, sends a canned test email (`ROLE_ADMIN`)

### Email (central on/off/test switch)

The Email tab under Settings (`/settings/email`, migration V39) is a single gate every outgoing email passes through, checked inside `EmailService.send()` itself rather than at each call site — so any future feature that sends email inherits it automatically, with no per-feature wiring. Backed by two `app_setting` rows: `email.mode` (`DISABLED` / `LIVE` / `TEST`) and `email.test-address`.

- **Disabled** (the default on a fresh deploy) — blocks every send, including the Gmail tab's own Send Test Email button. Fails with a clear 400 rather than silently dropping the email.
- **Live** — sends normally to the real recipient.
- **Test** — every email is redirected to the configured `email.test-address` instead of its real recipient, content unchanged (no "would have gone to" annotation) — lets you verify email content/formatting before real people receive anything.

No dedicated endpoints — both settings are read/written through the existing generic `GET /api/settings` / `PUT /api/settings/{key}`, same as any other setting; the Email tab is just a purpose-built radio-group UI over those two keys (the generic Settings → General page deliberately excludes `email.*` keys, same as it already does for `gmail.*`/`calendar.*`).

---

## Building

The project ships with **Maven Wrapper** (`mvnw` / `mvnw.cmd`).  
On first run the wrapper downloads Maven 3.9.6 automatically into your local Maven cache (`~/.m2/wrapper/`). No system-wide Maven installation is needed.

```bash
# Compile and run all tests
./mvnw verify

# Build the executable fat JAR (skip tests for speed)
./mvnw clean package -DskipTests

# The JAR is produced at:
#   target/svirerp-1.0.0-SNAPSHOT.jar
```

The fat JAR produced by `spring-boot-maven-plugin` embeds Tomcat and all dependencies. It is fully self-contained — no application server required.

### Release build (UI + API in one jar)

In production, the Spring Boot app serves the Angular build itself as static resources, so UI and API share one origin — no separate reverse proxy needed. This is a two-command build; `mvnw` does **not** invoke `npm` itself:

```bash
# 1. Build the Angular production bundle
cd ui && npm run build && cd ..

# 2. Build the jar — a Maven step copies ui/dist/svirerp-ui/browser into the
#    jar automatically if present. A backend-only build (skip step 1) still
#    works fine; the jar just won't have a UI to serve.
./mvnw clean package -DskipTests

java -jar target/svirerp-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
# → http://localhost:8080 now serves both the Angular app and /api/**
```

### Regenerate the Maven Wrapper

If you need to upgrade the bundled Maven version:

```bash
./mvnw wrapper:wrapper -Dmaven=3.9.6
```

---

## Database Migrations

All schema changes are managed by **Flyway**. Migration scripts live in `src/main/resources/db/migration/` and follow the naming convention `V{n}__{description}.sql`.

**Flyway runs automatically** every time the Spring Boot application starts. It applies any pending migrations in version order before the application accepts requests.

### First-time database setup

```bash
# Create the schema (replace credentials as needed)
mysql -u root -p -e "CREATE DATABASE svirerp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Run migrations with the Maven plugin (standalone — no app start)

The Flyway Maven plugin lets you apply or inspect migrations without starting the Spring Boot context, which is useful for CI pipelines and first-time setup.

```bash
# Apply all pending migrations
./mvnw flyway:migrate \
  -Dflyway.url="jdbc:mysql://localhost:3306/svirerp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" \
  -Dflyway.user=root \
  -Dflyway.password=your_password

# Check migration status (applied vs pending)
./mvnw flyway:info \
  -Dflyway.url="jdbc:mysql://..." \
  -Dflyway.user=root \
  -Dflyway.password=your_password

# Validate that applied checksums match the scripts on disk
./mvnw flyway:validate \
  -Dflyway.url="jdbc:mysql://..." \
  -Dflyway.user=root \
  -Dflyway.password=your_password
```

> **Tip:** The connection parameters can also be set permanently in `~/.m2/settings.xml` or passed as environment variables `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_PASSWORD` to avoid repeating them on every invocation.

### Adding a new migration

1. Create `src/main/resources/db/migration/V40__your_description.sql` (check `db/migration/` for the current highest version first — don't assume it matches this doc)
2. Write your `ALTER TABLE` / `CREATE TABLE` / etc.
3. Update the corresponding JPA entity
4. Restart the app (or run `./mvnw flyway:migrate`) — Flyway applies it automatically

> **Working against a database migrated by a different branch?** If your local DB already has migrations applied that aren't in your current branch's `db/migration/` folder (e.g. after switching branches), Flyway fails with "Detected applied migration not resolved locally." Uncomment `spring.flyway.ignore-migration-patterns=*:missing` in `application-local.properties` and number your new migration past the DB's current highest applied version, not just this branch's highest local file.

> **Never edit an already-applied migration.** Flyway validates checksums and will refuse to start if a committed script has changed.

---

## Running the API

### Development (with Maven, hot-reload via DevTools if added)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### From the executable JAR (production-style)

```bash
java -jar target/svirerp-1.0.0-SNAPSHOT.jar

# With a specific profile
java -jar target/svirerp-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# Changing port without rebuilding
java -jar target/svirerp-1.0.0-SNAPSHOT.jar --server.port=9090

# Full production example using environment variables
SVIRERP_DB_URL="jdbc:mysql://db.prod:3306/svirerp?useSSL=true&serverTimezone=UTC" \
SVIRERP_DB_USER=svirerp \
SVIRERP_DB_PASSWORD=prod-secret \
java -jar target/svirerp-1.0.0-SNAPSHOT.jar
```

The API starts at `http://localhost:8080` (or `--server.port` value). All REST endpoints are under `/api/`.

---

## Production Access

`https://svirerp.svivanrilski.com` is the public URL for the running app. TLS termination and reverse proxying are handled by a **Caddy** instance (not part of this repo) running as a Windows service on the host machine, config at `H:\caddy-w\Caddyfile`:

```
svirerp.svivanrilski.com {
	reverse_proxy 172.21.43.55:8080
}
```

Caddy obtains and renews the Let's Encrypt certificate automatically — no cert files or manual renewal needed. This is the same Caddy instance already fronting `app.svivanrilski.com` (a separate ERPNext installation, unrelated to this app), just with an additional site block.

The app itself currently runs inside a WSL2 VM rather than natively on Windows, so the reverse-proxy target above is the VM's internal IP rather than `127.0.0.1` (WSL2's `localhostForwarding` only relays WSL ports to Windows' IPv6 loopback, not the IPv4 loopback Caddy would otherwise use). Because that internal IP changes on every WSL restart, a scheduled task (**"Update WSL IP for Caddy"**, running `H:\caddy-w\update-wsl-ip.ps1`) periodically rewrites any `172.x.x.x` address found anywhere in the Caddyfile to the VM's current IP and reloads Caddy — this keeps the `svirerp.svivanrilski.com` block in sync automatically alongside the pre-existing `app.svivanrilski.com` one, with no per-domain wiring needed.

Reloading after a manual Caddyfile edit: `caddy reload --config H:\caddy-w\Caddyfile` (or restart the `caddy` Windows service).

---

## Angular UI

The Angular 21 front-end lives in `ui/`. It runs separately from the Spring Boot API and communicates with it over HTTP.

```bash
cd ui
npm install          # first time only
npm start            # dev server at http://localhost:4200
npm run build        # production build → ui/dist/svirerp-ui/browser/
```

`npm start` proxies `/api`, `/oauth2`, `/login`, and `/logout` to `http://localhost:8080` (see `ui/proxy.conf.json`), so the browser sees the dev server as same-origin — matching production, where Spring Boot serves both the UI and the API from one origin. This matters for session-cookie-based login: cross-origin cookies would need extra `SameSite`/HTTPS handling that same-origin avoids entirely.

Visiting the app while logged out redirects to `/login` (Google sign-in). The break-glass local admin form lives at `/portal-access` — see [Authentication](#authentication).

---

## API Overview

All endpoints return JSON. Errors follow the envelope `{ timestamp, status, error, message }`.

| Domain | Base path | Notes |
|---|---|---|
| Auth | `GET /api/auth/me` | Current user `{ email, name, provider }`; 401 means not logged in |
| Persons | `GET/POST /api/persons` | `GET /api/persons/{id}`, `PUT`, `DELETE` |
| Organizations | `GET/POST /api/organizations` | Single-org-per-installation in practice; managed via the [Admin Settings](#admin-settings) Organization tab |
| Membership types | `GET /api/organizations/{id}/membership-types` | `POST/PUT /api/membership-types[/{id}]`, `DELETE /api/membership-types/{id}` |
| Members | `GET /api/organizations/{id}/members` | `?status=&membershipTypeId=` filters (combinable); `POST/PUT /api/members[/{id}]`, `DELETE`, `GET .../expired` |
| Member CSV import | `GET /api/organizations/{id}/members/import-template` | `POST .../members/import` (multipart CSV, best-effort per-row) |
| Member contributions | `GET /api/organizations/{id}/member-payments` | `?fromDate=` filter; `POST/PUT /api/member-payments[/{id}]`, `DELETE`; per-member history at `GET /api/members/{memberId}/payments` |
| Trustees | `GET /api/organizations/{id}/trustees` | `POST/PUT /api/trustees[/{id}]`, `DELETE`, `POST .../{id}/renew` (re-elects for a fresh 2-year term) |
| Committees | `GET/POST /api/committees` | |
| Meeting minutes | `GET /api/organizations/{id}/meeting-minutes` | `POST/PUT /api/meeting-minutes[/{id}]`, `DELETE` (cascades to its action items); org-level board/trustee meetings, not tied to a committee |
| Action items | `GET /api/meeting-minutes/{id}/action-items` | `POST/PUT /api/action-items[/{id}]`, `DELETE`; unpaginated list, optional trustee assignee |
| Calendar events | `GET/POST /api/organizations/{id}/events` | `?from=&to=` date filter; `PUT/DELETE /api/events/{id}` (delete cascades to church details/registrations/resources) |
| Church service details | `GET /api/events/{id}/church-details` | 1:1 with an event, 404 if not recorded; `POST/PUT/DELETE /api/church-events[/{id}]` |
| Event registrations | `GET /api/events/{id}/registrations` | `POST/PUT/DELETE /api/event-registrations[/{id}]`; at most one registration per (event, person) |
| Event resources | `GET /api/events/{id}/resources` | `POST/PUT/DELETE /api/event-resources[/{id}]` |
| Volunteers | `GET/POST /api/volunteers` | `GET /api/organizations/{id}/volunteers` for org-scoped list; supports `contactPerson` + `areas` |
| Volunteer areas | `GET/POST /api/organizations/{id}/volunteer-areas` | Org-scoped lookup (Construction, Cooking Crew, etc.); lazily seeded with 7 starter areas on first request; `POST /api/volunteer-areas` to add more |
| Volunteer hours | `GET/POST /api/volunteers/{id}/hours` | `GET …/total-approved` |
| Funds (Projects) | `GET /api/organizations/{id}/funds` | `POST/PUT/DELETE /api/funds[/{id}]`; `GET /api/funds/{id}/summary` — opening balance, total income, total expense, balance for the "project financial status" view |
| Accounts (Categories) | `GET /api/organizations/{id}/accounts` | `POST/PUT/DELETE /api/accounts[/{id}]`, `GET .../accounts/roots`; org's first request lazily seeds a default chart of accounts (see `FinanceService#seedDefaultChartOfAccounts`) |
| Vendors | `GET /api/organizations/{id}/vendors` | `POST/PUT/DELETE /api/vendors[/{id}]` — payees for expenses |
| Service requests | `GET /api/organizations/{id}/service-requests` | `POST/PUT/DELETE /api/service-requests[/{id}]`; `GET .../{id}/balance` — agreedAmount minus every posted payment tagged with the request |
| Income / expense transactions | `POST /api/organizations/{id}/income-transactions` | `POST .../expense-transactions` — the simple Record Income/Record Expense flow; builds a balanced, immediately-posted 2-line `JournalEntry` behind the scenes, no debit/credit UI needed |
| Journal entries | `GET /api/organizations/{id}/journal-entries` | `?fundId=&entryDateFrom=&entryDateTo=` filters; `POST/PUT /api/journal-entries[/{id}]`, `POST …/{id}/post?approvedBy=`, `GET …/{id}/lines` |
| Bank accounts | `GET/POST /api/bank-accounts` | |
| Reconciliations | `GET/POST /api/reconciliations` | |
| App settings (admin) | `GET /api/settings` | `PUT /api/settings/{key}`; `ROLE_ADMIN` only, `SECRET` values never returned |
| Gmail (admin) | `GET /api/settings/gmail/authorize-url` | `GET .../callback` (OAuth redirect target), `POST .../test-send`; `ROLE_ADMIN` only — see [Admin Settings](#admin-settings) |
| Google Calendar (admin) | `GET /api/settings/calendar/authorize-url` | `GET .../callback`, `POST .../test-connection`; `ROLE_ADMIN` only. One-way push only (ERP → Calendar, never the reverse) — see `CalendarEvent.publishToOfficial`/`publishToInternal` and their `google*SyncError` fields |
| Zeffy payment import | `POST /api/organizations/{id}/zeffy-imports/preview` | Multipart `.xlsx`/`.xls`/`.csv` — Zeffy's real export is an Excel spreadsheet, parsed via Apache POI (`.csv` also accepted); persists one row per line with a computed `outcome` (`ready`/`duplicate`/`skipped_status`/`unmapped_campaign`/`error`), no writes to `Person`/`Member`/`MemberPayment`/`JournalEntry` yet. `GET .../zeffy-imports`, `GET /api/zeffy-imports/{batchId}[/summary\|/rows]`, `POST /api/organizations/{id}/zeffy-imports/{batchId}/commit` — applies every still-eligible row, one DB transaction per row (`ZeffyImportRowApplier`) |
| Zeffy campaign mappings | `GET /api/organizations/{id}/zeffy-campaign-mappings` | `POST .../zeffy-campaign-mappings/bulk`, `DELETE /api/zeffy-campaign-mappings/{id}` — persists which `Fund` a Zeffy "Campaign Title" posts income to, so recurring campaigns don't need remapping every import |
| Recompute member tiers | `POST /api/organizations/{id}/members/recompute-tiers` | Re-runs Follower/Member/Benefactor tier computation for every member in the org — tier can go stale purely from elapsed time, not just new payments |

Pagination is available on all list endpoints via `?page=0&size=20&sort=field,asc`.

---

## Migration Reference

| Version | Table created |
|---|---|
| V1 | `person` |
| V2 | `organization` |
| V3 | `membership_type` |
| V4 | `member` |
| V5 | `member_payment` |
| V6 | `trustee` |
| V7 | `trustee_document` |
| V8 | `volunteer` |
| V9 | `volunteer_hour` |
| V10 | `committee` |
| V11 | `committee_member` |
| V12 | `committee_meeting` |
| V13 | `committee_resolution` |
| V14 | `calendar_event` + deferred FK on `volunteer_hour.event_id` |
| V15 | `church_event` |
| V16 | `event_registration` |
| V17 | `event_resource` |
| V18 | `fund` |
| V19 | `account` (self-referential parent FK) |
| V20 | `journal_entry` (dual FK to `person` for created_by / approved_by) |
| V21 | `journal_line` |
| V22 | `budget` |
| V23 | `bank_account` |
| V24 | `bank_transaction` |
| V25 | `bank_reconciliation` (`difference` is a MySQL generated column) |
| V26 | `reconciliation_item` |
| V27 | `membership_type` + `can_vote` column |
| V28 | `app_setting` |
| V29 | `meeting_minutes` |
| V30 | `action_item` (nullable FK to `trustee` — assignee is optional) |
| V31 | `app_setting` rows for Gmail OAuth (`gmail.oauth.client-id`/`client-secret`/`refresh-token`, `gmail.sender-address`) — no new table, reuses V28's `app_setting` |
| V32 | `volunteer.contact_person_id` (nullable FK to `person`, `ON DELETE SET NULL`) |
| V33 | `volunteer_area` (org-scoped lookup) |
| V34 | `volunteer_area_assignment` (many-to-many join, `volunteer` ↔ `volunteer_area`) |
| V35 | `calendar_event.publish_to_official`/`publish_to_internal` + their `google_*_event_id`/`google_*_sync_error` tracking columns; seeds `calendar.*` `app_setting` rows for the Google Calendar OAuth connection |
| V36 | `vendor` (org-scoped payee list for expenses) |
| V37 | `service_request` (pre-paid church services — weddings, baptisms, funerals, memorials — tracked independently of scheduling, optional FK to `church_event`) |
| V38 | `journal_entry` transaction tags: `payment_method`, `check_number`, and nullable FKs `payer_id`→`person`, `vendor_id`→`vendor`, `service_request_id`→`service_request`, `category_account_id`/`fund_id`→`account`/`fund` — denormalized so the Finance transaction list doesn't need to join `journal_line` |
| V39 | `app_setting` rows for the central email switch (`email.mode` defaulting to `DISABLED`, `email.test-address`) — no new table, reuses V28's `app_setting` |
| V40 | Widens `member_payment.payment_method`'s CHECK to accept `'zeffy'` (via `MODIFY COLUMN` — MariaDB 11.8 embeds this CHECK in the column definition itself, not as a droppable named table constraint) |
| V41 | `zeffy_campaign_mapping` (org-scoped Campaign Title → `fund` mapping), `zeffy_import_batch`, `zeffy_import_row` (preview/commit staging area and permanent audit trail for the Zeffy payment import, one row per spreadsheet line with a computed `outcome`) |

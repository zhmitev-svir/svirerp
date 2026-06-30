# SVIR ERP

Non-profit ERP system for the SVIR organization — Spring Boot 3 REST API backed by MySQL 8 with Flyway schema migrations.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Quick Start](#quick-start)
4. [Configuration](#configuration)
5. [Building](#building)
6. [Database Migrations](#database-migrations)
7. [Running the API](#running-the-api)
8. [Angular UI](#angular-ui)
9. [API Overview](#api-overview)
10. [Migration Reference](#migration-reference)

---

## Prerequisites

| Requirement | Minimum | Notes |
|---|---|---|
| Java JDK | 17 | `java -version` to confirm |
| MySQL | 8.0 | Must be running before starting the app |
| Maven | — | **Not required** — use the included wrapper (`mvnw`) |
| Node.js | 20 LTS | Only needed for the Angular UI |

---

## Project Structure

```
svirerp/
├── src/
│   └── main/
│       ├── java/org/svir/svirerp/
│       │   ├── common/          # GlobalExceptionHandler, ResourceNotFoundException
│       │   ├── person/          # Entity, Repository, Service, Controller
│       │   ├── organization/
│       │   ├── membership/
│       │   ├── governance/
│       │   ├── event/
│       │   ├── volunteer/
│       │   └── finance/
│       └── resources/
│           ├── application.properties              # Base config (env-var placeholders)
│           ├── application-local.properties.example  # Copy & fill for local dev
│           └── db/migration/                       # Flyway V1–V26 SQL scripts
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

# 3. Run Flyway migrations (creates all 26 tables)
./mvnw flyway:migrate -Dflyway.user=root -Dflyway.password=your_password

# 4. Build and run
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

1. Create `src/main/resources/db/migration/V27__your_description.sql`
2. Write your `ALTER TABLE` / `CREATE TABLE` / etc.
3. Update the corresponding JPA entity
4. Restart the app (or run `./mvnw flyway:migrate`) — Flyway applies V27 automatically

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

## Angular UI

The Angular 21 front-end lives in `ui/`. It runs separately from the Spring Boot API and communicates with it over HTTP.

```bash
cd ui
npm install          # first time only
npm start            # dev server at http://localhost:4200
npm run build        # production build → ui/dist/svirerp-ui/
```

The development API proxy is not yet configured; the UI calls `http://localhost:8080/api` directly (set in `ui/src/environments/environment.ts`).

---

## API Overview

All endpoints return JSON. Errors follow the envelope `{ timestamp, status, error, message }`.

| Domain | Base path | Notes |
|---|---|---|
| Persons | `GET/POST /api/persons` | `GET /api/persons/{id}`, `PUT`, `DELETE` |
| Organizations | `GET/POST /api/organizations` | |
| Membership types | `GET/POST /api/organizations/{id}/membership-types` | |
| Members | `GET/POST /api/members` | |
| Member payments | `GET/POST /api/member-payments` | |
| Trustees | `GET/POST /api/trustees` | |
| Committees | `GET/POST /api/committees` | |
| Calendar events | `GET/POST /api/organizations/{id}/events` | `?from=&to=` date filter |
| Volunteers | `GET/POST /api/volunteers` | |
| Volunteer hours | `GET/POST /api/volunteers/{id}/hours` | `GET …/total-approved` |
| Accounts | `GET/POST /api/accounts` | `GET /api/organizations/{id}/accounts/roots` |
| Journal entries | `GET/POST /api/journal-entries` | `POST …/{id}/post?approvedBy=` |
| Bank accounts | `GET/POST /api/bank-accounts` | |
| Reconciliations | `GET/POST /api/reconciliations` | |

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

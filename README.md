# SVIR ERP

Non-profit ERP system for the SVIR organization.

## Overview

This project manages:
- Organization information & membership
- Members, Trustees & Volunteers
- Committees & meetings
- Calendar & church events
- Double-entry fund accounting
- Bank accounts & reconciliation

## Tech Stack

- **Database**: PostgreSQL
- **Migrations**: Flyway
- **Build**: Maven

## Database Migrations

All migrations live in `src/main/resources/db/migration` and follow the Flyway naming convention:

```
V{version}__{description}.sql
```

### Migration Index

| Version | Description |
|---------|-------------|
| V1 | Create person table |
| V2 | Create organization table |
| V3 | Create membership types table |
| V4 | Create members table |
| V5 | Create member payments table |
| V6 | Create trustees table |
| V7 | Create trustee documents table |
| V8 | Create volunteers table |
| V9 | Create volunteer hours table |
| V10 | Create committees table |
| V11 | Create committee members table |
| V12 | Create committee meetings table |
| V13 | Create committee resolutions table |
| V14 | Create calendar events table |
| V15 | Create church events table |
| V16 | Create event registrations table |
| V17 | Create event resources table |
| V18 | Create funds table |
| V19 | Create chart of accounts table |
| V20 | Create journal entries table |
| V21 | Create journal lines table |
| V22 | Create budgets table |
| V23 | Create bank accounts table |
| V24 | Create bank transactions table |
| V25 | Create bank reconciliations table |
| V26 | Create reconciliation items table |

## Running Migrations

```bash
mvn flyway:migrate
```

Configure your database connection in `src/main/resources/application.properties`.

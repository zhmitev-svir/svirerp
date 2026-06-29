-- V1__create_person.sql
-- Central person record shared across members, trustees, volunteers, and event registrations.
-- MySQL 8: UUID stored as CHAR(36); DEFAULT (UUID()) requires MySQL 8.0.13+.

CREATE TABLE person (
    id               CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    first_name       VARCHAR(100)        NOT NULL,
    last_name        VARCHAR(100)        NOT NULL,
    email            VARCHAR(255)        UNIQUE,
    phone            VARCHAR(30),
    address_line1    VARCHAR(255),
    city             VARCHAR(100),
    state            VARCHAR(100),
    zip              VARCHAR(20),
    date_of_birth    DATE,
    created_at       DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_person_email     ON person (email);
CREATE INDEX idx_person_last_name ON person (last_name);

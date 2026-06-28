-- V1__create_person.sql
-- Central person record shared across members, trustees, volunteers, and event registrations

CREATE TABLE person (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name       VARCHAR(100)        NOT NULL,
    last_name        VARCHAR(100)        NOT NULL,
    email            VARCHAR(255)        UNIQUE,
    phone            VARCHAR(30),
    address_line1    VARCHAR(255),
    city             VARCHAR(100),
    state            VARCHAR(100),
    zip              VARCHAR(20),
    date_of_birth    DATE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_person_email     ON person (email);
CREATE INDEX idx_person_last_name ON person (last_name);

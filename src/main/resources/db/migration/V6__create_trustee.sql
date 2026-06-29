-- V6__create_trustee.sql
-- Board trustees with term tracking and officer designation.

CREATE TABLE trustee (
    id           CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    person_id    CHAR(36)            NOT NULL,
    org_id       CHAR(36)            NOT NULL,
    title        VARCHAR(100),
    role         VARCHAR(100)        NOT NULL,
    term_start   DATE                NOT NULL,
    term_end     DATE,
    is_active    BOOLEAN             NOT NULL DEFAULT TRUE,
    is_officer   BOOLEAN             NOT NULL DEFAULT FALSE,
    notes        TEXT,
    created_at   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trustee_person FOREIGN KEY (person_id) REFERENCES person (id)       ON DELETE RESTRICT,
    CONSTRAINT fk_trustee_org    FOREIGN KEY (org_id)    REFERENCES organization (id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_trustee_person ON trustee (person_id);
CREATE INDEX idx_trustee_org    ON trustee (org_id);
CREATE INDEX idx_trustee_active ON trustee (is_active);

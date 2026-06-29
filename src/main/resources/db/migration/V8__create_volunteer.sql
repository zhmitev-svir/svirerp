-- V8__create_volunteer.sql
-- Volunteer roster linked to a person record.

CREATE TABLE volunteer (
    id              CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    person_id       CHAR(36)            NOT NULL,
    org_id          CHAR(36)            NOT NULL,
    onboard_date    DATE                NOT NULL,
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    skills          TEXT,
    availability    VARCHAR(255),
    notes           TEXT,

    CONSTRAINT fk_volunteer_person FOREIGN KEY (person_id) REFERENCES person (id)      ON DELETE RESTRICT,
    CONSTRAINT fk_volunteer_org    FOREIGN KEY (org_id)    REFERENCES organization (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_volunteer_person ON volunteer (person_id);
CREATE INDEX idx_volunteer_org    ON volunteer (org_id);
CREATE INDEX idx_volunteer_active ON volunteer (is_active);

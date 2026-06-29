-- V10__create_committee.sql
-- Standing and ad hoc committees with lifecycle tracking.

CREATE TABLE committee (
    id                CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    org_id            CHAR(36)            NOT NULL,
    name              VARCHAR(150)        NOT NULL,
    description       TEXT,
    committee_type    VARCHAR(100),
    is_standing       BOOLEAN             NOT NULL DEFAULT TRUE,
    is_active         BOOLEAN             NOT NULL DEFAULT TRUE,
    formed_date       DATE,
    dissolved_date    DATE,
    meeting_schedule  VARCHAR(255),
    mandate           TEXT,
    created_at        DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_committee_name_org UNIQUE (org_id, name),
    CONSTRAINT fk_committee_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_committee_org    ON committee (org_id);
CREATE INDEX idx_committee_active ON committee (is_active);

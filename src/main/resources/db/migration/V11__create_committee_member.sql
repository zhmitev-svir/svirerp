-- V11__create_committee_member.sql
-- Associates persons with committees, preserving historical membership via date ranges.

CREATE TABLE committee_member (
    id              CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    committee_id    CHAR(36)            NOT NULL,
    person_id       CHAR(36)            NOT NULL,
    role            VARCHAR(100),
    start_date      DATE                NOT NULL,
    end_date        DATE,
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    is_chair        BOOLEAN             NOT NULL DEFAULT FALSE,
    notes           TEXT,

    CONSTRAINT fk_committee_member_committee FOREIGN KEY (committee_id) REFERENCES committee (id) ON DELETE CASCADE,
    CONSTRAINT fk_committee_member_person    FOREIGN KEY (person_id)    REFERENCES person (id)    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_committee_member_committee ON committee_member (committee_id);
CREATE INDEX idx_committee_member_person    ON committee_member (person_id);

-- V4__create_member.sql
-- Links a person to the organisation as a member under a specific membership type.
-- CHECK constraints are enforced in MySQL 8.0.16+.

CREATE TABLE member (
    id                   CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    person_id            CHAR(36)            NOT NULL,
    org_id               CHAR(36)            NOT NULL,
    membership_type_id   CHAR(36)            NOT NULL,
    member_number        VARCHAR(50)         UNIQUE,
    join_date            DATE                NOT NULL,
    expiry_date          DATE,
    status               VARCHAR(30)         NOT NULL DEFAULT 'active'
                             CHECK (status IN ('active', 'inactive', 'suspended', 'expired', 'pending')),
    email_opt_in         BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at           DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_member_person          FOREIGN KEY (person_id)          REFERENCES person (id)          ON DELETE RESTRICT,
    CONSTRAINT fk_member_org             FOREIGN KEY (org_id)             REFERENCES organization (id)    ON DELETE CASCADE,
    CONSTRAINT fk_member_membership_type FOREIGN KEY (membership_type_id) REFERENCES membership_type (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_member_person  ON member (person_id);
CREATE INDEX idx_member_org     ON member (org_id);
CREATE INDEX idx_member_type    ON member (membership_type_id);
CREATE INDEX idx_member_status  ON member (status);
CREATE INDEX idx_member_expiry  ON member (expiry_date);

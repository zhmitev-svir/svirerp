-- V4__create_member.sql
-- Links a person to the organization as a member under a specific membership type

CREATE TABLE member (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id            UUID                NOT NULL REFERENCES person (id) ON DELETE RESTRICT,
    org_id               UUID                NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    membership_type_id   UUID                NOT NULL REFERENCES membership_type (id) ON DELETE RESTRICT,
    member_number        VARCHAR(50)         UNIQUE,
    join_date            DATE                NOT NULL,
    expiry_date          DATE,
    status               VARCHAR(30)         NOT NULL DEFAULT 'active'
                             CHECK (status IN ('active', 'inactive', 'suspended', 'expired', 'pending')),
    email_opt_in         BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_member_person         ON member (person_id);
CREATE INDEX idx_member_org            ON member (org_id);
CREATE INDEX idx_member_type           ON member (membership_type_id);
CREATE INDEX idx_member_status         ON member (status);
CREATE INDEX idx_member_expiry         ON member (expiry_date);

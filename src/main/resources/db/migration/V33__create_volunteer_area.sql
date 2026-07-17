-- V33__create_volunteer_area.sql
-- Org-scoped lookup of volunteer service areas (Construction, Cooking Crew, etc.).
-- Not pre-seeded here (no org exists at migration time); the app lazily seeds
-- starter areas for an org the first time its area list is requested empty.

CREATE TABLE volunteer_area (
    id          CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    org_id      CHAR(36)     NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_volunteer_area_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE CASCADE,
    CONSTRAINT uq_volunteer_area_name_org UNIQUE (org_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_volunteer_area_org ON volunteer_area (org_id);

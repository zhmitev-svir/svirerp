-- V34__create_volunteer_area_assignment.sql
-- Many-to-many: a volunteer can be active in multiple service areas.

CREATE TABLE volunteer_area_assignment (
    volunteer_id CHAR(36) NOT NULL,
    area_id      CHAR(36) NOT NULL,

    PRIMARY KEY (volunteer_id, area_id),
    CONSTRAINT fk_vaa_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteer (id)      ON DELETE CASCADE,
    CONSTRAINT fk_vaa_area      FOREIGN KEY (area_id)      REFERENCES volunteer_area (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_vaa_area ON volunteer_area_assignment (area_id);

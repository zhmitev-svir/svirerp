-- V32__add_volunteer_contact_person.sql
-- Optional contact person for a volunteer (e.g. a family member or coordinator to reach instead of the volunteer directly).
--
-- Numbered from V32, not V27, because this branch (feature/volunteer) forked before the
-- import-efforts branch's V27-V31 (can_vote, app_setting, meeting_minutes, action_item,
-- gmail_settings) — those migrations don't exist in this branch's source tree at all, but
-- a shared dev database may already have them applied. See application-local.properties.example
-- for the ignore-migration-patterns setting needed to run against such a database.

ALTER TABLE volunteer
  ADD COLUMN contact_person_id CHAR(36) NULL AFTER person_id,
  ADD CONSTRAINT fk_volunteer_contact_person
    FOREIGN KEY (contact_person_id) REFERENCES person (id) ON DELETE SET NULL;

CREATE INDEX idx_volunteer_contact_person ON volunteer (contact_person_id);

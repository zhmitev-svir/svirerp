-- V35__add_calendar_sync_to_events.sql
-- One-way push of ERP events to two Google Calendars (official + internal).
-- google_*_event_id is set once a push succeeds (needed to target updates/
-- deletes at the right Google-side event); google_*_sync_error is non-null
-- only when the most recent push attempt failed, so a stale id + an error
-- both being present means "was synced, latest edit failed to push — retry
-- on next save."

ALTER TABLE calendar_event
    ADD COLUMN publish_to_official       BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN google_official_event_id  VARCHAR(255) NULL,
    ADD COLUMN google_official_sync_error TEXT        NULL,
    ADD COLUMN publish_to_internal       BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN google_internal_event_id  VARCHAR(255) NULL,
    ADD COLUMN google_internal_sync_error TEXT        NULL;

INSERT INTO app_setting (setting_key, value, value_type, description) VALUES
    ('calendar.oauth.client-id', NULL, 'STRING', 'Google Calendar OAuth 2.0 Client ID (Web application)'),
    ('calendar.oauth.client-secret', NULL, 'SECRET', 'Google Calendar OAuth 2.0 Client Secret'),
    ('calendar.oauth.refresh-token', NULL, 'SECRET', 'Google Calendar OAuth 2.0 refresh token — set automatically by the Connect Google Calendar flow'),
    ('calendar.official.id', NULL, 'STRING', 'Google Calendar ID for the official/public calendar'),
    ('calendar.internal.id', NULL, 'STRING', 'Google Calendar ID for the internal calendar');

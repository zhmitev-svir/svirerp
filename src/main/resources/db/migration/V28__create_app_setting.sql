CREATE TABLE app_setting (
    id           CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    setting_key  VARCHAR(100) NOT NULL UNIQUE,
    value        TEXT,
    value_type   VARCHAR(20)  NOT NULL
                     CHECK (value_type IN ('STRING', 'SECRET', 'BOOLEAN', 'NUMBER')),
    description  VARCHAR(255),
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO app_setting (setting_key, value, value_type, description) VALUES
    ('google.oauth.client-id', NULL, 'STRING', 'Google OAuth 2.0 Client ID'),
    ('google.oauth.client-secret', NULL, 'SECRET', 'Google OAuth 2.0 Client Secret');

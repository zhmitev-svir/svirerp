INSERT INTO app_setting (setting_key, value, value_type, description) VALUES
    ('gmail.oauth.client-id', NULL, 'STRING', 'Gmail OAuth 2.0 Client ID (Web application)'),
    ('gmail.oauth.client-secret', NULL, 'SECRET', 'Gmail OAuth 2.0 Client Secret'),
    ('gmail.oauth.refresh-token', NULL, 'SECRET', 'Gmail OAuth 2.0 refresh token — set automatically by the Connect Gmail flow'),
    ('gmail.sender-address', NULL, 'STRING', 'Gmail address emails are sent from — set automatically by the Connect Gmail flow');

INSERT INTO app_setting (setting_key, value, value_type, description) VALUES
    ('email.mode', 'DISABLED', 'STRING', 'Email sending mode: DISABLED, LIVE, or TEST'),
    ('email.test-address', NULL, 'STRING', 'When email.mode is TEST, every outgoing email is redirected here instead of the real recipient');

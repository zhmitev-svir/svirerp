package com.svivanrilski.svirerp.settings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Encrypts SECRET-type app_setting values at rest. Fails loudly rather than
 * silently if the key/salt aren't configured — same "opt-in" pattern as the
 * local admin login (see LocalAdminSecurityConfig) — since a SECRET setting
 * with no encryption configured has nowhere safe to be stored.
 */
@Component
public class SettingEncryptor {

    @Value("${app.settings.encryption-key}")
    private String encryptionKey;

    @Value("${app.settings.encryption-salt}")
    private String encryptionSalt;

    // Cached lazily: PBE key derivation is intentionally expensive, and the
    // Google client-secret lookup runs on every OAuth2 login attempt, not
    // just when an admin edits a setting.
    private volatile TextEncryptor cached;

    private TextEncryptor delegate() {
        TextEncryptor instance = cached;
        if (instance == null) {
            if (isBlank(encryptionKey) || isBlank(encryptionSalt)) {
                throw new IllegalStateException(
                        "app.settings.encryption-key and app.settings.encryption-salt must both be set to use SECRET settings");
            }
            instance = Encryptors.text(encryptionKey, encryptionSalt);
            cached = instance;
        }
        return instance;
    }

    public String encrypt(String plainText) {
        return delegate().encrypt(plainText);
    }

    public String decrypt(String cipherText) {
        return delegate().decrypt(cipherText);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

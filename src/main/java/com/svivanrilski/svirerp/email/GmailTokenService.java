package com.svivanrilski.svirerp.email;

import com.svivanrilski.svirerp.settings.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * Exchanges the admin-configured Gmail refresh token for short-lived access
 * tokens, the same "resolve credentials from app_setting at call time, not
 * application.properties" pattern SecurityConfig uses for Google SSO login —
 * so a rotated client secret or a re-run of the Connect Gmail flow takes
 * effect immediately with no restart.
 */
@Service
@RequiredArgsConstructor
public class GmailTokenService {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";

    // Renew a little early rather than racing an access token's real expiry.
    private static final long EXPIRY_BUFFER_SECONDS = 60;

    private final AppSettingService settingService;
    private final RestClient restClient = RestClient.create();

    private volatile String cachedAccessToken;
    private volatile Instant cachedAccessTokenExpiry = Instant.EPOCH;

    public synchronized String getAccessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(cachedAccessTokenExpiry)) {
            return cachedAccessToken;
        }

        String clientId = settingService.getDecryptedValue("gmail.oauth.client-id")
                .orElseThrow(() -> new IllegalArgumentException("Gmail client ID is not configured"));
        String clientSecret = settingService.getDecryptedValue("gmail.oauth.client-secret")
                .orElseThrow(() -> new IllegalArgumentException("Gmail client secret is not configured"));
        String refreshToken = settingService.getDecryptedValue("gmail.oauth.refresh-token")
                .orElseThrow(() -> new IllegalArgumentException("Gmail account is not connected yet — use Connect Gmail in Settings"));

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("client_id=" + encode(clientId)
                        + "&client_secret=" + encode(clientSecret)
                        + "&refresh_token=" + encode(refreshToken)
                        + "&grant_type=refresh_token")
                .retrieve()
                .body(Map.class);

        String accessToken = (String) response.get("access_token");
        int expiresIn = ((Number) response.get("expires_in")).intValue();

        cachedAccessToken = accessToken;
        cachedAccessTokenExpiry = Instant.now().plusSeconds(Math.max(0, expiresIn - EXPIRY_BUFFER_SECONDS));
        return accessToken;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

package com.svivanrilski.svirerp.common;

import com.svivanrilski.svirerp.settings.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic Google OAuth refresh-token -> access-token exchange, shared by any
 * integration that stores its own client-id/secret/refresh-token as
 * AppSetting rows (Gmail, Calendar, ...) and just needs a short-lived access
 * token at call time. Extracted from GmailTokenService so a second
 * integration (Calendar) doesn't duplicate the same ~30 lines of RestClient
 * boilerplate and in-memory caching.
 */
@Service
@RequiredArgsConstructor
public class GoogleOAuthTokenService {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";

    // Renew a little early rather than racing an access token's real expiry.
    private static final long EXPIRY_BUFFER_SECONDS = 60;

    private final AppSettingService settingService;
    private final RestClient restClient = RestClient.create();

    // Keyed by refreshTokenKey so different integrations' cached access
    // tokens never collide even though they share this one service instance.
    private final ConcurrentHashMap<String, CachedToken> cache = new ConcurrentHashMap<>();

    public String getAccessToken(String clientIdKey, String clientSecretKey, String refreshTokenKey) {
        CachedToken cached = cache.get(refreshTokenKey);
        if (cached != null && Instant.now().isBefore(cached.expiry)) {
            return cached.accessToken;
        }

        String clientId = settingService.getDecryptedValue(clientIdKey)
                .orElseThrow(() -> new IllegalArgumentException("Client ID is not configured (" + clientIdKey + ")"));
        String clientSecret = settingService.getDecryptedValue(clientSecretKey)
                .orElseThrow(() -> new IllegalArgumentException("Client secret is not configured (" + clientSecretKey + ")"));
        String refreshToken = settingService.getDecryptedValue(refreshTokenKey)
                .orElseThrow(() -> new IllegalArgumentException("Not connected yet (" + refreshTokenKey + ") — use Connect in Settings"));

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

        CachedToken fresh = new CachedToken(accessToken,
                Instant.now().plusSeconds(Math.max(0, expiresIn - EXPIRY_BUFFER_SECONDS)));
        cache.put(refreshTokenKey, fresh);
        return accessToken;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record CachedToken(String accessToken, Instant expiry) {
    }
}

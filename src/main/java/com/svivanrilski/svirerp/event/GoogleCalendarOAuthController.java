package com.svivanrilski.svirerp.event;

import com.svivanrilski.svirerp.common.GoogleOAuthTokenService;
import com.svivanrilski.svirerp.settings.AppSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One-time "Connect Google Calendar" admin flow — same shape as
 * GmailOAuthController, but a fully independent connection (own
 * calendar.oauth.* credentials/refresh token) rather than sharing Gmail's,
 * so reconnecting or breaking one can never affect the other. Falls under
 * the existing /api/settings/** ROLE_ADMIN matcher, no SecurityConfig change
 * needed.
 */
@RestController
@RequestMapping("/api/settings/calendar")
@RequiredArgsConstructor
public class GoogleCalendarOAuthController {

    private static final String AUTHORIZATION_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String CALENDARS_URI = "https://www.googleapis.com/calendar/v3/calendars";
    private static final String SCOPE = "https://www.googleapis.com/auth/calendar.events";

    private final AppSettingService settingService;
    private final GoogleOAuthTokenService tokenService;
    private final RestClient restClient = RestClient.create();

    @GetMapping("/authorize-url")
    public Map<String, String> authorizeUrl(HttpServletRequest request) {
        String clientId = settingService.getDecryptedValue("calendar.oauth.client-id")
                .orElseThrow(() -> new IllegalArgumentException("Set the Calendar Client ID before connecting"));

        String url = UriComponentsBuilder.fromUriString(AUTHORIZATION_URI)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri(request))
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();
        return Map.of("url", url);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam(required = false) String code,
                          @RequestParam(required = false) String error,
                          HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        if (error != null) {
            response.sendRedirect("/settings/calendar?calendar_error=" + UriUtils.encode(error, StandardCharsets.UTF_8));
            return;
        }

        try {
            String clientId = settingService.getDecryptedValue("calendar.oauth.client-id")
                    .orElseThrow(() -> new IllegalStateException("Calendar Client ID is not configured"));
            String clientSecret = settingService.getDecryptedValue("calendar.oauth.client-secret")
                    .orElseThrow(() -> new IllegalStateException("Calendar Client Secret is not configured"));

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restClient.post()
                    .uri(TOKEN_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("client_id=" + encode(clientId)
                            + "&client_secret=" + encode(clientSecret)
                            + "&code=" + encode(code)
                            + "&redirect_uri=" + encode(redirectUri(request))
                            + "&grant_type=authorization_code")
                    .retrieve()
                    .body(Map.class);

            String refreshToken = (String) tokenResponse.get("refresh_token");
            if (refreshToken == null) {
                response.sendRedirect("/settings/calendar?calendar_error=no_refresh_token");
                return;
            }

            settingService.updateValue("calendar.oauth.refresh-token", refreshToken);

            response.sendRedirect("/settings/calendar?calendar_connected=true");
        } catch (Exception e) {
            response.sendRedirect("/settings/calendar?calendar_error=connect_failed");
        }
    }

    /**
     * Read-only check that the connected account can see each configured
     * calendar ID — lighter than actually creating a throwaway event on a
     * real calendar, the way Gmail's test-send sends a real email.
     */
    @PostMapping("/test-connection")
    public Map<String, String> testConnection() {
        Map<String, String> results = new LinkedHashMap<>();
        checkCalendar("calendar.official.id", "official", results);
        checkCalendar("calendar.internal.id", "internal", results);
        return results;
    }

    private void checkCalendar(String settingKey, String label, Map<String, String> results) {
        String calendarId = settingService.getDecryptedValue(settingKey).orElse(null);
        if (calendarId == null) {
            results.put(label, "Not configured");
            return;
        }
        try {
            String accessToken = tokenService.getAccessToken(
                    "calendar.oauth.client-id", "calendar.oauth.client-secret", "calendar.oauth.refresh-token");
            @SuppressWarnings("unchecked")
            Map<String, Object> calendar = restClient.get()
                    .uri(CALENDARS_URI + "/{calendarId}", calendarId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
            results.put(label, "OK — " + calendar.get("summary"));
        } catch (Exception e) {
            results.put(label, "Failed: " + e.getMessage());
        }
    }

    private String redirectUri(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromContextPath(request)
                .path("/api/settings/calendar/callback")
                .build()
                .toUriString();
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

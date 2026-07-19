package com.svivanrilski.svirerp.email;

import com.svivanrilski.svirerp.settings.AppSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * One-time "Connect Gmail" admin flow: falls under the existing /api/settings/**
 * ROLE_ADMIN matcher in SecurityConfig, so no security-config change is needed.
 * Captures a refresh token for the Gmail API's gmail.send scope and stores it
 * via AppSettingService, the same encrypted app_setting table Google SSO's own
 * client-id/secret already live in.
 */
@RestController
@RequestMapping("/api/settings/gmail")
@RequiredArgsConstructor
@Slf4j
public class GmailOAuthController {

    private static final String AUTHORIZATION_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String SCOPE =
            "https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/userinfo.email";

    private final AppSettingService settingService;
    private final EmailService emailService;
    private final RestClient restClient = RestClient.create();

    @GetMapping("/authorize-url")
    public Map<String, String> authorizeUrl(HttpServletRequest request) {
        String clientId = settingService.getDecryptedValue("gmail.oauth.client-id")
                .orElseThrow(() -> new IllegalArgumentException("Set the Gmail Client ID before connecting"));

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
            response.sendRedirect("/settings/gmail?gmail_error=" + UriUtils.encode(error, StandardCharsets.UTF_8));
            return;
        }

        try {
            String clientId = settingService.getDecryptedValue("gmail.oauth.client-id")
                    .orElseThrow(() -> new IllegalStateException("Gmail Client ID is not configured"));
            String clientSecret = settingService.getDecryptedValue("gmail.oauth.client-secret")
                    .orElseThrow(() -> new IllegalStateException("Gmail Client Secret is not configured"));

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
                response.sendRedirect("/settings/gmail?gmail_error=no_refresh_token");
                return;
            }
            String accessToken = (String) tokenResponse.get("access_token");

            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = restClient.get()
                    .uri(USERINFO_URI)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
            String senderAddress = (String) userInfo.get("email");

            settingService.updateValue("gmail.oauth.refresh-token", refreshToken);
            if (senderAddress != null) {
                settingService.updateValue("gmail.sender-address", senderAddress);
            }

            response.sendRedirect("/settings/gmail?gmail_connected=true");
        } catch (Exception e) {
            log.error("Gmail OAuth callback failed", e);
            response.sendRedirect("/settings/gmail?gmail_error=connect_failed");
        }
    }

    @PostMapping(value = "/test-send", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void testSend(@RequestBody TestSendRequest request) {
        emailService.send(request.to(), "SVIR ERP test email",
                "<p>This is a test email sent from SVIR ERP's Gmail integration.</p>");
    }

    private String redirectUri(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromContextPath(request)
                .path("/api/settings/gmail/callback")
                .build()
                .toUriString();
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record TestSendRequest(String to) {
    }
}

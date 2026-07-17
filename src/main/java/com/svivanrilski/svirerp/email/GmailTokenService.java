package com.svivanrilski.svirerp.email;

import com.svivanrilski.svirerp.common.GoogleOAuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Exchanges the admin-configured Gmail refresh token for short-lived access
 * tokens, the same "resolve credentials from app_setting at call time, not
 * application.properties" pattern SecurityConfig uses for Google SSO login —
 * so a rotated client secret or a re-run of the Connect Gmail flow takes
 * effect immediately with no restart. Delegates the actual token exchange to
 * GoogleOAuthTokenService, shared with the Calendar integration.
 */
@Service
@RequiredArgsConstructor
public class GmailTokenService {

    private final GoogleOAuthTokenService tokenService;

    public String getAccessToken() {
        return tokenService.getAccessToken(
                "gmail.oauth.client-id", "gmail.oauth.client-secret", "gmail.oauth.refresh-token");
    }
}

package org.svir.svirerp.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Restricts Google sign-in to the org's Google Workspace hosted domain.
 * This is defense-in-depth on top of setting the OAuth consent screen's
 * User Type to "Internal" in Google Cloud Console (which restricts sign-in
 * to the Workspace org before the app ever sees the request) — kept here in
 * case that consent-screen setting is ever missed or changed.
 */
@Service
public class GoogleHostedDomainOidcUserService extends OidcUserService {

    @Value("${app.auth.google.allowed-domain}")
    private String allowedDomain;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String hostedDomain = oidcUser.getIdToken().getClaimAsString("hd");
        if (!allowedDomain.equalsIgnoreCase(hostedDomain)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("hd_mismatch"),
                    "Account is not part of the authorized organization");
        }

        return oidcUser;
    }
}

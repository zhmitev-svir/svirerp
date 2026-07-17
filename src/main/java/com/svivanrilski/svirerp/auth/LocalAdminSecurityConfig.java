package com.svivanrilski.svirerp.auth;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Builds the break-glass local admin AuthenticationProvider, wrapping the
 * standard DaoAuthenticationProvider with lockout checks and audit logging.
 * Returns empty when app.auth.admin.username/password-hash aren't both set,
 * so an unconfigured deployment doesn't expose a broken login path.
 */
@Component
@RequiredArgsConstructor
public class LocalAdminSecurityConfig {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT.local-admin-login");

    private final LoginAttemptService loginAttemptService;

    @Value("${app.auth.admin.username}")
    private String adminUsername;

    @Value("${app.auth.admin.password-hash}")
    private String adminPasswordHash;

    public Optional<AuthenticationProvider> buildProvider() {
        if (isBlank(adminUsername) || isBlank(adminPasswordHash)) {
            return Optional.empty();
        }

        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(adminPasswordHash)
                        .roles("ADMIN")
                        .build());

        DaoAuthenticationProvider delegate = new DaoAuthenticationProvider(new BCryptPasswordEncoder());
        delegate.setUserDetailsService(userDetailsManager);

        return Optional.of(new LockoutAwareAuthenticationProvider(delegate));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private class LockoutAwareAuthenticationProvider implements AuthenticationProvider {

        private final DaoAuthenticationProvider delegate;

        private LockoutAwareAuthenticationProvider(DaoAuthenticationProvider delegate) {
            this.delegate = delegate;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String remoteAddress = currentRemoteAddress();
            String attemptedUsername = String.valueOf(authentication.getName());

            if (loginAttemptService.isLockedOut(remoteAddress)) {
                AUDIT_LOG.warn("Local admin login LOCKED_OUT ip={} username={}", remoteAddress, attemptedUsername);
                throw new LockedException("Too many failed attempts — try again later");
            }

            try {
                Authentication result = delegate.authenticate(authentication);
                loginAttemptService.recordSuccess(remoteAddress);
                AUDIT_LOG.info("Local admin login SUCCESS ip={} username={}", remoteAddress, attemptedUsername);
                return result;
            } catch (BadCredentialsException ex) {
                loginAttemptService.recordFailure(remoteAddress);
                AUDIT_LOG.warn("Local admin login FAILURE ip={} username={}", remoteAddress, attemptedUsername);
                throw ex;
            }
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return delegate.supports(authentication);
        }

        private String currentRemoteAddress() {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                return attrs.getRequest().getRemoteAddr();
            }
            return "unknown";
        }
    }
}

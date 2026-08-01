package com.svivanrilski.svirerp.stripeintegration;

import com.stripe.exception.SignatureVerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

/**
 * A bad/forged signature must never reach {@link StripeWebhookService#handleWebhook} as a "valid"
 * event — this is the endpoint's only authentication, since it's otherwise unauthenticated (see
 * SecurityConfig). A validly-signed request must return 200 regardless of what processing did
 * internally (StripeWebhookService itself is responsible for not letting business failures
 * propagate — see StripeWebhookEventApplierTest).
 */
@ExtendWith(MockitoExtension.class)
class StripeWebhookControllerTest {

    @Mock
    private StripeWebhookService service;

    @InjectMocks
    private StripeWebhookController controller;

    @Test
    void handleWebhook_invalidSignature_returns400() throws Exception {
        doThrow(new SignatureVerificationException("bad signature", "sig_header"))
                .when(service).handleWebhook("payload", "bad-sig");

        ResponseEntity<Void> response = controller.handleWebhook("payload", "bad-sig");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleWebhook_validSignature_returns200() throws Exception {
        doNothing().when(service).handleWebhook("payload", "good-sig");

        ResponseEntity<Void> response = controller.handleWebhook("payload", "good-sig");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void handleWebhook_secretsNotConfigured_returns503() throws Exception {
        doThrow(new IllegalStateException("Stripe webhook signing secret is not configured"))
                .when(service).handleWebhook("payload", "good-sig");

        ResponseEntity<Void> response = controller.handleWebhook("payload", "good-sig");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}

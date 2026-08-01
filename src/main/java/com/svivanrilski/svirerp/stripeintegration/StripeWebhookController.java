package com.svivanrilski.svirerp.stripeintegration;

import com.stripe.exception.SignatureVerificationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeWebhookService service;

    /**
     * Unauthenticated by design (see SecurityConfig) — Stripe calls this server-to-server with no
     * session/CSRF token, authenticated instead by the signature in Stripe-Signature. Always
     * returns 200 for a validly-signed, recognized event even when applying it failed for a
     * business reason (see StripeWebhookService#handleWebhook) — Stripe would otherwise keep
     * retrying for up to ~3 days, which wouldn't fix a bad/missing-data problem; the event is
     * instead flagged for an admin to fix and reprocess. Only a bad signature (400), the secret(s)
     * not being configured yet (503 — an admin needs to fill in Settings > Stripe), or a genuine
     * infrastructure failure (500, via GlobalExceptionHandler) should make Stripe retry.
     */
    @PostMapping("/api/webhooks/stripe")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            service.handleWebhook(payload, sigHeader);
        } catch (SignatureVerificationException e) {
            log.warn("Rejected Stripe webhook with invalid signature: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("Stripe webhook received but not fully configured: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok().build();
    }
}

package com.svivanrilski.svirerp.stripeintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Admin CRUD for Stripe product mappings and the webhook event log — ordinary authenticated
 *  {@code /api/**} endpoints, unlike {@link StripeWebhookController}'s unauthenticated receiver. */
@RestController
@RequiredArgsConstructor
public class StripeIntegrationController {

    private final StripeWebhookService service;

    // ── Product mappings ─────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/stripe-product-mappings")
    public List<StripeProductMapping> listMappings(@PathVariable UUID orgId) {
        return service.findMappingsByOrg(orgId);
    }

    /** Prices pulled live from the connected Stripe account, so an admin can create a mapping
     *  before that product has ever actually been paid for through svirerp. */
    @GetMapping("/api/organizations/{orgId}/stripe-prices")
    public List<StripeWebhookService.StripePriceInfo> listStripePrices(@PathVariable UUID orgId) {
        return service.listStripePrices(orgId);
    }

    @PostMapping("/api/organizations/{orgId}/stripe-product-mappings")
    public ResponseEntity<StripeProductMapping> createMapping(@PathVariable UUID orgId,
            @RequestBody StripeWebhookService.StripeProductMappingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMapping(orgId, request));
    }

    @PutMapping("/api/stripe-product-mappings/{id}")
    public StripeProductMapping updateMapping(@PathVariable UUID id,
            @RequestBody StripeWebhookService.StripeProductMappingRequest request) {
        return service.updateMapping(id, request);
    }

    @DeleteMapping("/api/stripe-product-mappings/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable UUID id) {
        service.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }

    // ── Events ───────────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/stripe-events")
    public Page<StripeWebhookEvent> listEvents(@PathVariable UUID orgId, Pageable pageable) {
        return service.findEventsByOrg(orgId, pageable);
    }

    @PostMapping("/api/stripe-events/{id}/reprocess")
    public StripeWebhookEvent reprocess(@PathVariable UUID id) {
        return service.reprocessEvent(id);
    }
}

package com.svivanrilski.svirerp.stripeintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.svivanrilski.svirerp.finance.Account;
import com.svivanrilski.svirerp.finance.FinanceService;
import com.svivanrilski.svirerp.finance.JournalEntry;
import com.svivanrilski.svirerp.finance.RecordIncomeRequest;
import com.svivanrilski.svirerp.finance.ServiceRequest;
import com.svivanrilski.svirerp.membership.Member;
import com.svivanrilski.svirerp.membership.MemberPayment;
import com.svivanrilski.svirerp.membership.MemberPaymentRepository;
import com.svivanrilski.svirerp.membership.MembershipService;
import com.svivanrilski.svirerp.organization.Organization;
import com.svivanrilski.svirerp.person.Person;
import com.svivanrilski.svirerp.person.PersonService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

/**
 * Applies a single {@link StripeWebhookEvent} — called by {@link StripeWebhookService}, a
 * different bean, so {@link #applyEvent} always gets its own fresh transaction whether it succeeds
 * or throws. Same idiom as ZeffyImportRowApplier: on failure the caller catches the exception and
 * calls {@link #markEventError}, itself a separate transaction, so the event's terminal status is
 * always durably recorded even though the failed attempt's domain writes rolled back.
 */
@Service
@RequiredArgsConstructor
public class StripeWebhookEventApplier {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 500;
    private static final ZoneId CHURCH_ZONE = ZoneId.of("America/Chicago");

    private final StripeWebhookEventRepository eventRepo;
    private final StripeProductMappingRepository mappingRepo;
    private final PersonService personService;
    private final MembershipService membershipService;
    private final MemberPaymentRepository memberPaymentRepo;
    private final FinanceService financeService;

    /** Returns null (no-op) if this event id was already recorded — either an earlier delivery
     *  already inserted the row, or a concurrent duplicate delivery won the race on the DB's unique
     *  constraint on stripe_event_id. */
    @Transactional
    public StripeWebhookEvent recordReceived(Organization org, String stripeEventId, String eventType,
            String stripePriceId, BigDecimal amount, String email, String firstName, String lastName,
            String payload) {
        if (eventRepo.existsByStripeEventId(stripeEventId)) {
            return null;
        }
        try {
            return eventRepo.save(StripeWebhookEvent.builder()
                    .org(org)
                    .stripeEventId(stripeEventId)
                    .eventType(eventType)
                    .stripePriceId(stripePriceId)
                    .amount(amount)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .payload(payload)
                    .status("received")
                    .build());
        } catch (DataIntegrityViolationException e) {
            return null;
        }
    }

    /**
     * Dispatches by the mapped Price's purpose. Idempotent against retries: a row already
     * 'processed' or 'ignored' is a no-op, so redelivering an event (or manually reprocessing one
     * already handled) does no harm. 'needs_mapping'/'error' rows are retried — this is exactly
     * what backs the admin "Reprocess" action once a missing mapping has been added.
     */
    @Transactional
    public void applyEvent(UUID eventRowId) {
        StripeWebhookEvent row = eventRepo.findById(eventRowId)
                .orElseThrow(() -> new IllegalArgumentException("Stripe webhook event not found: " + eventRowId));
        if ("processed".equals(row.getStatus()) || "ignored".equals(row.getStatus())) {
            return;
        }

        UUID orgId = row.getOrg().getId();

        if (row.getStripePriceId() == null || row.getStripePriceId().isBlank()) {
            row.setStatus("needs_mapping");
            row.setErrorMessage("No Stripe Price ID could be resolved from this event");
            eventRepo.save(row);
            return;
        }

        Optional<StripeProductMapping> mappingOpt =
                mappingRepo.findByOrgIdAndStripePriceId(orgId, row.getStripePriceId());
        if (mappingOpt.isEmpty()) {
            row.setStatus("needs_mapping");
            row.setErrorMessage(null);
            eventRepo.save(row);
            return;
        }
        StripeProductMapping mapping = mappingOpt.get();

        if (row.getEmail() == null || row.getEmail().isBlank()) {
            throw new IllegalArgumentException("Stripe event has no customer email — cannot match/create a person");
        }
        if (row.getAmount() == null) {
            throw new IllegalArgumentException("Stripe event is missing an amount");
        }

        boolean isNewPerson = personService.findByEmailIfExists(row.getEmail()).isEmpty();
        Person person = isNewPerson
                ? personService.create(Person.builder()
                        .firstName(row.getFirstName() != null ? row.getFirstName() : "Unknown")
                        .lastName(row.getLastName() != null ? row.getLastName() : "Unknown")
                        .email(row.getEmail())
                        .build())
                : personService.findByEmail(row.getEmail());

        LocalDate paymentDate = LocalDate.now(CHURCH_ZONE);
        Account depositAccount = resolveDepositAccount(orgId);
        Account categoryAccount = resolveCategoryAccount(mapping, orgId);
        UUID fundId = mapping.getFund() != null ? mapping.getFund().getId() : null;

        Member member = null;
        MemberPayment payment = null;
        ServiceRequest serviceRequest = null;

        switch (mapping.getPurpose()) {
            case "membership_dues" -> {
                member = membershipService.findOrCreateFollowerMember(person.getId(), orgId, paymentDate);
                payment = memberPaymentRepo.save(MemberPayment.builder()
                        .member(member)
                        .amount(row.getAmount())
                        .paymentDate(paymentDate)
                        .paymentMethod("stripe")
                        .transactionRef(row.getStripeEventId())
                        .status("completed")
                        .notes(buildNotes(mapping))
                        .build());
                member = membershipService.recomputeTier(member.getId());
            }
            case "service_request" -> serviceRequest = financeService.createServiceRequest(ServiceRequest.builder()
                    .org(row.getOrg())
                    .requestorPerson(person)
                    .serviceType(mapping.getServiceType() != null ? mapping.getServiceType() : "other")
                    .agreedAmount(row.getAmount())
                    .status("requested")
                    .notes(buildNotes(mapping))
                    .build());
            default -> {
                // event_ticket / general_income — nothing beyond the income posting below
            }
        }

        JournalEntry entry = financeService.recordIncome(new RecordIncomeRequest(
                orgId,
                paymentDate,
                row.getAmount(),
                buildDescription(mapping),
                categoryAccount.getId(),
                depositAccount.getId(),
                fundId,
                person.getId(),
                serviceRequest != null ? serviceRequest.getId() : null,
                "stripe",
                null));

        row.setPerson(person);
        row.setMember(member);
        row.setMemberPayment(payment);
        row.setServiceRequest(serviceRequest);
        row.setJournalEntry(entry);
        row.setStatus("processed");
        row.setProcessedAt(OffsetDateTime.now());
        row.setErrorMessage(null);
        eventRepo.save(row);
    }

    /** Runs in its own transaction so it still commits even though the failed applyEvent() call rolled back. */
    @Transactional
    public void markEventError(UUID eventRowId, String message) {
        StripeWebhookEvent row = eventRepo.findById(eventRowId)
                .orElseThrow(() -> new IllegalArgumentException("Stripe webhook event not found: " + eventRowId));
        row.setStatus("error");
        row.setErrorMessage(truncate(message));
        eventRepo.save(row);
    }

    /** A recognized event type that {@link StripeWebhookService} decided isn't actionable (e.g. a
     *  Checkout-created PaymentIntent's companion event) — recorded with a reason rather than left
     *  with no trace at all, so an admin can see why nothing was posted for it. */
    @Transactional
    public void markIgnored(UUID eventRowId, String reason) {
        StripeWebhookEvent row = eventRepo.findById(eventRowId)
                .orElseThrow(() -> new IllegalArgumentException("Stripe webhook event not found: " + eventRowId));
        row.setStatus("ignored");
        row.setErrorMessage(truncate(reason));
        eventRepo.save(row);
    }

    private Account resolveDepositAccount(UUID orgId) {
        financeService.findAccountsByOrg(orgId, PageRequest.of(0, 1)); // triggers the lazy chart-of-accounts seed
        return financeService.findAccountByNumber(orgId, "1010");
    }

    /** Falls back to a sensible default revenue account per purpose when the mapping doesn't
     *  specify one — an admin can always pick something more specific in the mapping's own Account
     *  field once they've created it via the existing Categories (Account) screen. */
    private Account resolveCategoryAccount(StripeProductMapping mapping, UUID orgId) {
        if (mapping.getCategoryAccount() != null) {
            return mapping.getCategoryAccount();
        }
        String fallbackNumber = switch (mapping.getPurpose()) {
            case "membership_dues" -> "4000";
            case "service_request" -> "4030";
            default -> "4090";
        };
        return financeService.findAccountByNumber(orgId, fallbackNumber);
    }

    private String buildNotes(StripeProductMapping mapping) {
        return "Paid via Stripe" + (mapping.getDisplayName() != null && !mapping.getDisplayName().isBlank()
                ? " — " + mapping.getDisplayName() : "");
    }

    private String buildDescription(StripeProductMapping mapping) {
        return mapping.getDisplayName() != null && !mapping.getDisplayName().isBlank()
                ? "Stripe: " + mapping.getDisplayName()
                : "Stripe payment";
    }

    private String truncate(String detail) {
        if (detail == null) return null;
        return detail.length() > ERROR_MESSAGE_MAX_LENGTH ? detail.substring(0, ERROR_MESSAGE_MAX_LENGTH) : detail;
    }
}

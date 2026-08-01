package com.svivanrilski.svirerp.stripeintegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Covers StripeWebhookEventApplier's purpose-dispatch branches — the core logic that turns a
 * parsed Stripe webhook event into Person/Member/MemberPayment/ServiceRequest/JournalEntry writes.
 * All collaborators are mocked; this deliberately doesn't stand up a Spring context or a real DB.
 */
@ExtendWith(MockitoExtension.class)
class StripeWebhookEventApplierTest {

    @Mock private StripeWebhookEventRepository eventRepo;
    @Mock private StripeProductMappingRepository mappingRepo;
    @Mock private PersonService personService;
    @Mock private MembershipService membershipService;
    @Mock private MemberPaymentRepository memberPaymentRepo;
    @Mock private FinanceService financeService;

    @InjectMocks
    private StripeWebhookEventApplier applier;

    private Organization org;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        org = new Organization();
        org.setId(orgId);
    }

    private StripeWebhookEvent baseEvent(String status) {
        StripeWebhookEvent event = new StripeWebhookEvent();
        event.setId(UUID.randomUUID());
        event.setOrg(org);
        event.setStripeEventId("evt_" + UUID.randomUUID());
        event.setEventType("checkout.session.completed");
        event.setStripePriceId("price_123");
        event.setAmount(new BigDecimal("150.00"));
        event.setEmail("payer@example.com");
        event.setFirstName("Jane");
        event.setLastName("Doe");
        event.setPayload("{}");
        event.setStatus(status);
        return event;
    }

    @Test
    void applyEvent_alreadyProcessed_isNoOp() {
        StripeWebhookEvent event = baseEvent("processed");
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        applier.applyEvent(event.getId());

        verifyNoInteractions(mappingRepo, personService, membershipService, memberPaymentRepo, financeService);
        verify(eventRepo, never()).save(any());
    }

    @Test
    void applyEvent_priceNotMapped_marksNeedsMapping() {
        StripeWebhookEvent event = baseEvent("received");
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));
        when(mappingRepo.findByOrgIdAndStripePriceId(orgId, "price_123")).thenReturn(Optional.empty());

        applier.applyEvent(event.getId());

        ArgumentCaptor<StripeWebhookEvent> captor = ArgumentCaptor.forClass(StripeWebhookEvent.class);
        verify(eventRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("needs_mapping");
        verifyNoInteractions(personService, membershipService, memberPaymentRepo, financeService);
    }

    @Test
    void applyEvent_noPriceIdResolved_marksNeedsMapping() {
        StripeWebhookEvent event = baseEvent("received");
        event.setStripePriceId(null);
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        applier.applyEvent(event.getId());

        ArgumentCaptor<StripeWebhookEvent> captor = ArgumentCaptor.forClass(StripeWebhookEvent.class);
        verify(eventRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("needs_mapping");
        verifyNoInteractions(mappingRepo);
    }

    @Test
    void applyEvent_membershipDues_createsPaymentAndRecomputesTier() {
        StripeWebhookEvent event = baseEvent("received");
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        StripeProductMapping mapping = new StripeProductMapping();
        mapping.setPurpose("membership_dues");
        when(mappingRepo.findByOrgIdAndStripePriceId(orgId, "price_123")).thenReturn(Optional.of(mapping));

        when(personService.findByEmailIfExists("payer@example.com")).thenReturn(Optional.empty());
        Person person = new Person();
        person.setId(UUID.randomUUID());
        when(personService.create(any(Person.class))).thenReturn(person);

        Member member = new Member();
        member.setId(UUID.randomUUID());
        when(membershipService.findOrCreateFollowerMember(eq(person.getId()), eq(orgId), any(LocalDate.class)))
                .thenReturn(member);

        MemberPayment payment = MemberPayment.builder().member(member).amount(event.getAmount()).build();
        when(memberPaymentRepo.save(any(MemberPayment.class))).thenReturn(payment);

        Member recomputed = new Member();
        recomputed.setId(member.getId());
        when(membershipService.recomputeTier(member.getId())).thenReturn(recomputed);

        when(financeService.findAccountsByOrg(eq(orgId), any(PageRequest.class))).thenReturn(Page.empty());
        Account depositAccount = new Account();
        depositAccount.setId(UUID.randomUUID());
        Account categoryAccount = new Account();
        categoryAccount.setId(UUID.randomUUID());
        when(financeService.findAccountByNumber(orgId, "1010")).thenReturn(depositAccount);
        when(financeService.findAccountByNumber(orgId, "4000")).thenReturn(categoryAccount);

        JournalEntry entry = new JournalEntry();
        entry.setId(UUID.randomUUID());
        when(financeService.recordIncome(any(RecordIncomeRequest.class))).thenReturn(entry);

        applier.applyEvent(event.getId());

        ArgumentCaptor<RecordIncomeRequest> reqCaptor = ArgumentCaptor.forClass(RecordIncomeRequest.class);
        verify(financeService).recordIncome(reqCaptor.capture());
        assertThat(reqCaptor.getValue().paymentMethod()).isEqualTo("stripe");
        assertThat(reqCaptor.getValue().amount()).isEqualByComparingTo("150.00");
        assertThat(reqCaptor.getValue().payerId()).isEqualTo(person.getId());
        assertThat(reqCaptor.getValue().serviceRequestId()).isNull();

        ArgumentCaptor<MemberPayment> paymentCaptor = ArgumentCaptor.forClass(MemberPayment.class);
        verify(memberPaymentRepo).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getPaymentMethod()).isEqualTo("stripe");
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo("completed");

        verify(membershipService).recomputeTier(member.getId());

        ArgumentCaptor<StripeWebhookEvent> eventCaptor = ArgumentCaptor.forClass(StripeWebhookEvent.class);
        verify(eventRepo).save(eventCaptor.capture());
        StripeWebhookEvent saved = eventCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("processed");
        assertThat(saved.getPerson()).isEqualTo(person);
        assertThat(saved.getMemberPayment()).isEqualTo(payment);
        assertThat(saved.getJournalEntry()).isEqualTo(entry);
        assertThat(saved.getServiceRequest()).isNull();
        assertThat(saved.getProcessedAt()).isNotNull();
    }

    @Test
    void applyEvent_serviceRequestPurpose_createsServiceRequest() {
        StripeWebhookEvent event = baseEvent("received");
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        StripeProductMapping mapping = new StripeProductMapping();
        mapping.setPurpose("service_request");
        mapping.setServiceType("wedding");
        when(mappingRepo.findByOrgIdAndStripePriceId(orgId, "price_123")).thenReturn(Optional.of(mapping));

        Person person = new Person();
        person.setId(UUID.randomUUID());
        when(personService.findByEmailIfExists(anyString())).thenReturn(Optional.of(person));
        when(personService.findByEmail(anyString())).thenReturn(person);

        ServiceRequest serviceRequest = ServiceRequest.builder().id(UUID.randomUUID()).build();
        when(financeService.createServiceRequest(any(ServiceRequest.class))).thenReturn(serviceRequest);

        when(financeService.findAccountsByOrg(eq(orgId), any(PageRequest.class))).thenReturn(Page.empty());
        Account depositAccount = new Account();
        depositAccount.setId(UUID.randomUUID());
        Account categoryAccount = new Account();
        categoryAccount.setId(UUID.randomUUID());
        when(financeService.findAccountByNumber(orgId, "1010")).thenReturn(depositAccount);
        when(financeService.findAccountByNumber(orgId, "4030")).thenReturn(categoryAccount);

        JournalEntry entry = new JournalEntry();
        entry.setId(UUID.randomUUID());
        when(financeService.recordIncome(any(RecordIncomeRequest.class))).thenReturn(entry);

        applier.applyEvent(event.getId());

        ArgumentCaptor<ServiceRequest> srCaptor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(financeService).createServiceRequest(srCaptor.capture());
        assertThat(srCaptor.getValue().getServiceType()).isEqualTo("wedding");
        assertThat(srCaptor.getValue().getRequestorPerson()).isEqualTo(person);

        ArgumentCaptor<RecordIncomeRequest> reqCaptor = ArgumentCaptor.forClass(RecordIncomeRequest.class);
        verify(financeService).recordIncome(reqCaptor.capture());
        assertThat(reqCaptor.getValue().serviceRequestId()).isEqualTo(serviceRequest.getId());

        verifyNoInteractions(membershipService, memberPaymentRepo);
    }

    @Test
    void applyEvent_missingEmail_throws() {
        StripeWebhookEvent event = baseEvent("received");
        event.setEmail(null);
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        StripeProductMapping mapping = new StripeProductMapping();
        mapping.setPurpose("general_income");
        when(mappingRepo.findByOrgIdAndStripePriceId(orgId, "price_123")).thenReturn(Optional.of(mapping));

        assertThrows(IllegalArgumentException.class, () -> applier.applyEvent(event.getId()));

        verify(eventRepo, never()).save(any());
    }

    @Test
    void markEventError_truncatesLongMessages() {
        StripeWebhookEvent event = baseEvent("received");
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));

        String longMessage = "x".repeat(600);
        applier.markEventError(event.getId(), longMessage);

        ArgumentCaptor<StripeWebhookEvent> captor = ArgumentCaptor.forClass(StripeWebhookEvent.class);
        verify(eventRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("error");
        assertThat(captor.getValue().getErrorMessage()).hasSize(500);
    }
}

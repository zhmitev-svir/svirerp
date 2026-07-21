package com.svivanrilski.svirerp.zeffyimport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.svivanrilski.svirerp.finance.FinanceService;
import com.svivanrilski.svirerp.finance.Fund;
import com.svivanrilski.svirerp.finance.JournalEntry;
import com.svivanrilski.svirerp.finance.RecordIncomeRequest;
import com.svivanrilski.svirerp.membership.Member;
import com.svivanrilski.svirerp.membership.MemberPayment;
import com.svivanrilski.svirerp.membership.MemberPaymentRepository;
import com.svivanrilski.svirerp.membership.MembershipService;
import com.svivanrilski.svirerp.person.Person;
import com.svivanrilski.svirerp.person.PersonService;

import java.util.UUID;

/**
 * Applies a single {@link ZeffyImportRow} — called once per row by {@link ZeffyImportService}, a
 * different bean, so each row gets its own transaction (same idiom as
 * MembershipService#importOrUpdateMemberFromRow / MemberImportService: a shared big transaction
 * risks Hibernate marking the whole batch rollback-only after one bad row).
 */
@Service
@RequiredArgsConstructor
public class ZeffyImportRowApplier {

    private static final int OUTCOME_DETAIL_MAX_LENGTH = 500;

    private final ZeffyImportRowRepository rowRepo;
    private final ZeffyCampaignMappingRepository mappingRepo;
    private final PersonService personService;
    private final MembershipService membershipService;
    private final MemberPaymentRepository memberPaymentRepo;
    private final FinanceService financeService;

    /**
     * Only rows still in outcome 'ready' or 'unmapped_campaign' are processed (idempotent no-op
     * otherwise — e.g. if commit is retried after a partial failure). A campaign mapping added
     * after preview is re-checked here, since commit is the authoritative point of no return.
     */
    @Transactional
    public void applyRow(UUID rowId, UUID depositAccountId, UUID categoryAccountId) {
        ZeffyImportRow row = rowRepo.findById(rowId)
                .orElseThrow(() -> new IllegalArgumentException("Zeffy import row not found: " + rowId));
        if (!"ready".equals(row.getOutcome()) && !"unmapped_campaign".equals(row.getOutcome())) {
            return;
        }

        Fund fund = row.getFund();
        if (row.getCampaignTitle() != null && !row.getCampaignTitle().isBlank() && fund == null) {
            fund = mappingRepo.findByOrgIdAndCampaignTitleIgnoreCase(row.getOrg().getId(), row.getCampaignTitle())
                    .map(ZeffyCampaignMapping::getFund)
                    .orElse(null);
            if (fund == null) {
                row.setOutcome("unmapped_campaign");
                row.setOutcomeDetail(row.getCampaignTitle());
                rowRepo.save(row);
                return;
            }
        }

        if (row.getEmail() == null || row.getEmail().isBlank()) {
            throw new IllegalArgumentException("Row " + row.getRowNumber() + " has no email — cannot match/create a person");
        }
        if (row.getAmount() == null || row.getPaymentDate() == null) {
            throw new IllegalArgumentException("Row " + row.getRowNumber() + " is missing amount or payment date");
        }

        boolean isNewPerson = personService.findByEmailIfExists(row.getEmail()).isEmpty();
        Person person = isNewPerson
                ? personService.create(Person.builder()
                        .firstName(row.getFirstName() != null ? row.getFirstName() : "Unknown")
                        .lastName(row.getLastName() != null ? row.getLastName() : "Unknown")
                        .email(row.getEmail())
                        .addressLine1(row.getAddress())
                        .city(row.getCity())
                        .state(row.getState())
                        .zip(row.getPostalCode())
                        .build())
                : personService.fillBlankFields(
                        personService.findByEmail(row.getEmail()).getId(),
                        Person.builder()
                                .addressLine1(row.getAddress())
                                .city(row.getCity())
                                .state(row.getState())
                                .zip(row.getPostalCode())
                                .build());

        boolean isNewMember = !membershipService.hasMembership(person.getId(), row.getOrg().getId());
        Member member = membershipService.findOrCreateFollowerMember(
                person.getId(), row.getOrg().getId(), row.getPaymentDate());

        MemberPayment payment = memberPaymentRepo.save(MemberPayment.builder()
                .member(member)
                .amount(row.getAmount())
                .paymentDate(row.getPaymentDate())
                .paymentMethod("zeffy")
                .transactionRef(row.getTaxReceiptNumber())
                .status("completed")
                .notes(buildNotes(row))
                .build());

        Member recomputed = membershipService.recomputeTier(member.getId());

        // A $0 row (e.g. a free-RSVP Zeffy signup) has no money to post — FinanceService.recordIncome
        // requires a strictly positive debit/credit on a real journal line (chk_journal_line_one_side),
        // and there's genuinely nothing to record on the Finance page for it. Still create the
        // Person/Member/MemberPayment above so it counts toward tier computation.
        JournalEntry entry = row.getAmount().signum() > 0
                ? financeService.recordIncome(new RecordIncomeRequest(
                        row.getOrg().getId(),
                        row.getPaymentDate(),
                        row.getAmount(),
                        buildDescription(row),
                        categoryAccountId,
                        depositAccountId,
                        fund != null ? fund.getId() : null,
                        person.getId(),
                        null,
                        "zeffy",
                        null))
                : null;

        row.setPerson(person);
        row.setMember(recomputed);
        row.setMemberPayment(payment);
        row.setJournalEntry(entry);
        row.setFund(fund);
        row.setIsNewPerson(isNewPerson);
        row.setIsNewMember(isNewMember);
        row.setOutcome("committed");
        row.setOutcomeDetail(null);
        rowRepo.save(row);
    }

    /** Runs in its own transaction so it still commits even though the failed applyRow() call rolled back. */
    @Transactional
    public void markRowError(UUID rowId, String detail) {
        ZeffyImportRow row = rowRepo.findById(rowId)
                .orElseThrow(() -> new IllegalArgumentException("Zeffy import row not found: " + rowId));
        row.setOutcome("error");
        row.setOutcomeDetail(truncate(detail));
        rowRepo.save(row);
    }

    private String buildNotes(ZeffyImportRow row) {
        StringBuilder notes = new StringBuilder("Imported from Zeffy");
        if (row.getCampaignTitle() != null && !row.getCampaignTitle().isBlank()) {
            notes.append(" — campaign: ").append(row.getCampaignTitle());
        }
        if (row.getTaxReceiptUrl() != null && !row.getTaxReceiptUrl().isBlank()) {
            notes.append(" — receipt: ").append(row.getTaxReceiptUrl());
        }
        return notes.toString();
    }

    private String buildDescription(ZeffyImportRow row) {
        return (row.getCampaignTitle() != null && !row.getCampaignTitle().isBlank())
                ? "Zeffy: " + row.getCampaignTitle()
                : "Zeffy donation";
    }

    private String truncate(String detail) {
        if (detail == null) return null;
        return detail.length() > OUTCOME_DETAIL_MAX_LENGTH ? detail.substring(0, OUTCOME_DETAIL_MAX_LENGTH) : detail;
    }
}

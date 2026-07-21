package com.svivanrilski.svirerp.zeffyimport;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.svivanrilski.svirerp.finance.Fund;
import com.svivanrilski.svirerp.finance.JournalEntry;
import com.svivanrilski.svirerp.membership.Member;
import com.svivanrilski.svirerp.membership.MemberPayment;
import com.svivanrilski.svirerp.organization.Organization;
import com.svivanrilski.svirerp.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One row per line of an uploaded Zeffy CSV. Persisted at preview time (before anything else is
 * written) with a computed {@link #outcome}; commit only applies rows still in {@code ready}.
 * This table is both the preview/commit staging area and the permanent import audit trail.
 */
@Entity
@Table(name = "zeffy_import_row")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZeffyImportRow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ZeffyImportBatch batch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    // Column is "csv_row_number", not "row_number" — ROW_NUMBER is a reserved MariaDB 10.2+ keyword.
    @Column(name = "csv_row_number", nullable = false)
    private Integer rowNumber;

    // --- Raw/parsed Zeffy CSV columns ---

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    /** Raw "Payment Time (America/Chicago)" text — kept unparsed, only used for the dedupe key. */
    @Column(name = "payment_time", length = 20)
    private String paymentTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    /** Raw "Payment Status" text from the export (e.g. "Succeeded", "Refunded"). */
    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "payout_date")
    private LocalDate payoutDate;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 50)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "tax_receipt_number", length = 50)
    private String taxReceiptNumber;

    @Column(name = "tax_receipt_url", length = 500)
    private String taxReceiptUrl;

    @Column(name = "campaign_title", length = 255)
    private String campaignTitle;

    // --- Computed during preview ---

    /** Tax Receipt # when present, else "email|paymentDate|paymentTime|amount". */
    @Column(name = "dedupe_key", length = 500)
    private String dedupeKey;

    /**
     * Allowed values (DB CHECK): pending_preview, ready, duplicate, skipped_status,
     * unmapped_campaign, error, committed.
     */
    @Column(nullable = false, length = 30)
    private String outcome = "pending_preview";

    @Column(name = "outcome_detail", length = 500)
    private String outcomeDetail;

    @Column(name = "is_new_person", nullable = false)
    private Boolean isNewPerson = false;

    @Column(name = "is_new_member", nullable = false)
    private Boolean isNewMember = false;

    // --- Stamped on commit ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_payment_id")
    private MemberPayment memberPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id")
    private Fund fund;
}

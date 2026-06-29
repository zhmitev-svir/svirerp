package org.svir.svirerp.finance;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Individual debit or credit line belonging to a journal entry.
 * Exactly one of debit_amount or credit_amount must be non-zero (DB CHECK chk_journal_line_one_side).
 * This constraint is also enforced in the service layer before persisting.
 */
@Entity
@Table(name = "journal_line")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Nullable: lines not tied to a specific fund are treated as unrestricted. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(0)
    @Column(name = "debit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Min(0)
    @Column(name = "credit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(length = 255)
    private String memo;
}

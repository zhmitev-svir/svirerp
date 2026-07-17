package com.svivanrilski.svirerp.finance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Raw bank transaction imported from a statement or entered manually. */
@Entity
@Table(name = "bank_transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    /** Nullable: linked to a journal entry once the transaction has been coded in the GL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** Allowed values (DB CHECK): debit, credit, check, transfer, fee, interest, other. */
    @NotNull
    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String payee;

    @Column(name = "check_number", length = 20)
    private String checkNumber;

    /** Allowed values (DB CHECK): pending, cleared, void. */
    @Column(nullable = false, length = 30)
    private String status = "pending";

    @Column(name = "is_reconciled", nullable = false)
    private Boolean isReconciled = false;

    @Column(name = "imported_at", nullable = false, updatable = false)
    private OffsetDateTime importedAt;

    @PrePersist
    private void prePersist() {
        if (importedAt == null) importedAt = OffsetDateTime.now();
    }
}

package org.svir.svirerp.finance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.svir.svirerp.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Formal monthly reconciliation session comparing statement balance to book balance.
 *
 * The 'difference' column is a MySQL 8 GENERATED ALWAYS AS (statement_balance - book_balance) STORED
 * column (V25 migration).  insertable=false + updatable=false instructs Hibernate to omit it from
 * INSERT and UPDATE statements so MySQL computes it.  Call entityManager.refresh() after save()
 * if you need the computed value within the same transaction.
 */
@Entity
@Table(
    name = "bank_reconciliation",
    uniqueConstraints = @UniqueConstraint(name = "uq_reconciliation_account_date",
        columnNames = {"bank_account_id", "statement_date"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciled_by")
    private Person reconciledBy;

    @NotNull
    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @NotNull
    @Column(name = "statement_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal statementBalance;

    @NotNull
    @Column(name = "book_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal bookBalance;

    /**
     * Computed by MySQL as (statement_balance - book_balance).
     * Never set this field in Java — it will be populated on SELECT.
     */
    @Column(name = "difference", insertable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal difference;

    /** Allowed values (DB CHECK): in_progress, completed, discrepancy. */
    @Column(nullable = false, length = 30)
    private String status = "in_progress";

    @Column(name = "reconciled_at")
    private OffsetDateTime reconciledAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

package org.svir.svirerp.finance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/** Line item within a reconciliation session marking each transaction as cleared or outstanding. */
@Entity
@Table(
    name = "reconciliation_item",
    uniqueConstraints = @UniqueConstraint(name = "uq_reconciliation_item",
        columnNames = {"reconciliation_id", "bank_transaction_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false)
    private BankReconciliation reconciliation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_transaction_id", nullable = false)
    private BankTransaction bankTransaction;

    @Column(name = "is_cleared", nullable = false)
    private Boolean isCleared = false;

    /** Allowed values (DB CHECK): transaction, deposit_in_transit, outstanding_check, adjustment. */
    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType = "transaction";

    @Column(columnDefinition = "TEXT")
    private String notes;
}

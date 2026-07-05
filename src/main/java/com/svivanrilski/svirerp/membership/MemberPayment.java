package org.svir.svirerp.membership;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Records each dues or fee payment made by a member. */
@Entity
@Table(name = "member_payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /**
     * Allowed values (DB CHECK): cash, check, credit_card, ach, online, other.
     * Stored as String to match the VARCHAR column.
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    /** Allowed values (DB CHECK): pending, completed, failed, refunded. */
    @Column(nullable = false, length = 30)
    private String status = "completed";

    @Column(columnDefinition = "TEXT")
    private String notes;
}

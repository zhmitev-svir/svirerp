package com.svivanrilski.svirerp.membership;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Derives a member's tier from their completed-payment history. Stateless — shared by the real
 * tier recompute ({@link MembershipService#recomputeTier}) and the Zeffy import preview, which
 * simulates the tier a new/updated payment would produce before anything is committed.
 *
 * <p>Tiers, low to high: Follower (any payment, including $0) &lt; Member ($150+, grants voting
 * rights — VOTING_THRESHOLD) &lt; Benefactor ($1000+). "Member" is this org's pre-existing name
 * for the $150+ tier — not to be confused with the generic domain concept of a "Member" (any
 * {@code membership.Member} row, regardless of tier).
 *
 * <p>Rule: the highest *current* tier is the highest one for which a qualifying payment exists
 * within its 1-year window; Follower is a non-expiring baseline — once any completed payment
 * exists (including $0), the member never drops out of a tier entirely, just down to Follower.
 *
 * <p>{@link TierResult#expiryDate()} is a separate concern from the current tier: it's always the
 * last-ever $150+-or-above payment date plus one year, computed independently of the 1-year
 * window and therefore independently of whether the member's *current* tier is still
 * Member/Benefactor or has already lapsed to Follower. This is deliberate — an admin needs to see
 * when a lapsed paying member's status actually expired (a past date), not just null once they've
 * fallen back to Follower. A member who was never a $150+ payer gets a null expiryDate.
 */
public final class TierCalculator {

    public static final String BENEFACTOR = "Benefactor";
    public static final String MEMBER = "Member";
    public static final String FOLLOWER = "Follower";

    public static final BigDecimal BENEFACTOR_THRESHOLD = new BigDecimal("1000.00");
    public static final BigDecimal VOTING_THRESHOLD = new BigDecimal("150.00");

    /** Dates on Zeffy's export are already Chicago-local calendar dates; window math uses the same zone. */
    private static final ZoneId WINDOW_ZONE = ZoneId.of("America/Chicago");

    private TierCalculator() {
    }

    public record PaymentSnapshot(BigDecimal amount, LocalDate paymentDate) {
    }

    /** Null if there is no completed-payment history at all (caller decides how to treat that). */
    public record TierResult(String tierName, LocalDate expiryDate) {
    }

    public static TierResult compute(List<PaymentSnapshot> completedPayments) {
        if (completedPayments == null || completedPayments.isEmpty()) {
            return null;
        }

        LocalDate windowStart = LocalDate.now(WINDOW_ZONE).minusYears(1);
        boolean anyCompletedPayment = false;
        boolean benefactorWithinWindow = false;
        boolean votingWithinWindow = false;
        // Tracked regardless of the 1-year window, purely to derive expiryDate — see class doc.
        LocalDate latestVotingPaymentEver = null;

        for (PaymentSnapshot payment : completedPayments) {
            if (payment.amount() == null || payment.paymentDate() == null) continue;
            anyCompletedPayment = true;

            boolean withinWindow = !payment.paymentDate().isBefore(windowStart);
            boolean isBenefactorLevel = payment.amount().compareTo(BENEFACTOR_THRESHOLD) >= 0;
            boolean isVotingLevel = payment.amount().compareTo(VOTING_THRESHOLD) >= 0;

            if (isBenefactorLevel && withinWindow) benefactorWithinWindow = true;
            if (isVotingLevel && withinWindow) votingWithinWindow = true;
            if (isVotingLevel
                    && (latestVotingPaymentEver == null || payment.paymentDate().isAfter(latestVotingPaymentEver))) {
                latestVotingPaymentEver = payment.paymentDate();
            }
        }

        LocalDate expiryDate = latestVotingPaymentEver != null ? latestVotingPaymentEver.plusYears(1) : null;

        if (benefactorWithinWindow) {
            return new TierResult(BENEFACTOR, expiryDate);
        }
        if (votingWithinWindow) {
            return new TierResult(MEMBER, expiryDate);
        }
        if (anyCompletedPayment) {
            return new TierResult(FOLLOWER, expiryDate);
        }
        return null;
    }
}

package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;
import java.util.UUID;

/** One fund's financial status, for the all-funds-at-once Funds Overview report — same math as
 *  FundSummary/FinanceService#fundSummary, batched across every active fund instead of one at a time. */
public record FundOverviewRow(
        UUID fundId,
        String fundName,
        String fundType,
        BigDecimal openingBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {
}

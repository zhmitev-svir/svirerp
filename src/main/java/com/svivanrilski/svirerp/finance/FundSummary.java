package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;

/** A project/fund's financial status: opening balance plus everything posted against it. */
public record FundSummary(
        BigDecimal openingBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {
}

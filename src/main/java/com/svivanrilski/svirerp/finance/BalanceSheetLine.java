package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;
import java.util.UUID;

/** One account row on the Statement of Financial Position — balance is always a positive dollar
 *  figure (already sign-adjusted for the account's normal balance by
 *  FinanceService#statementOfFinancialPosition). */
public record BalanceSheetLine(
        UUID accountId,
        String accountNumber,
        String accountName,
        BigDecimal balance) {
}

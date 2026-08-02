package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;
import java.util.UUID;

/** One category row on the Statement of Activities — amount is always a positive dollar figure
 *  (already sign-adjusted for the account's normal balance by FinanceService#statementOfActivities). */
public record StatementOfActivitiesLine(
        UUID accountId,
        String accountNumber,
        String accountName,
        BigDecimal amount) {
}

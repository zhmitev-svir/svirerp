package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for the simple "Record Income" flow — donations, membership dues, fundraising and
 * service payments received via Zeffy, cash, or check. FinanceService#recordIncome turns this into
 * a balanced, posted JournalEntry with two JournalLines.
 */
public record RecordIncomeRequest(
        UUID orgId,
        LocalDate entryDate,
        BigDecimal amount,
        String description,
        UUID categoryAccountId,
        UUID depositAccountId,
        UUID fundId,
        UUID payerId,
        UUID serviceRequestId,
        String paymentMethod,
        String checkNumber) {
}

package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Income vs. expense by category for a period — "how did this period go." */
public record StatementOfActivities(
        LocalDate from,
        LocalDate to,
        List<StatementOfActivitiesLine> income,
        BigDecimal totalIncome,
        List<StatementOfActivitiesLine> expense,
        BigDecimal totalExpense,
        BigDecimal netChange) {
}

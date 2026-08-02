package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Every account's balance as of a date, grouped Assets / Liabilities / Equity — "where does our
 * money sit right now." {@code netIncomeToDate} is a computed, not stored, line (this app has no
 * period-closing mechanism that zeroes revenue/expense into equity) — see
 * FinanceService#statementOfFinancialPosition. By the ordinary double-entry identity,
 * {@code totalAssets} always equals {@code totalLiabilitiesAndEquity}.
 */
public record StatementOfFinancialPosition(
        LocalDate asOf,
        List<BalanceSheetLine> assets,
        BigDecimal totalAssets,
        List<BalanceSheetLine> liabilities,
        BigDecimal totalLiabilities,
        List<BalanceSheetLine> equity,
        BigDecimal netIncomeToDate,
        BigDecimal totalEquity,
        BigDecimal totalLiabilitiesAndEquity) {
}

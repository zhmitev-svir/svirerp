package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;
import java.util.UUID;

/** Spring Data interface projection for a per-account debit-minus-credit aggregate — see
 *  JournalLineRepository#sumByAccountForOrgAndTypeAndDateRange / #sumByAccountForOrgAndTypeAsOfDate. */
public interface AccountAmount {
    UUID getAccountId();
    String getAccountNumber();
    String getAccountName();
    BigDecimal getAmount();
}

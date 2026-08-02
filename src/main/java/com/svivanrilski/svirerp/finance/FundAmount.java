package com.svivanrilski.svirerp.finance;

import java.math.BigDecimal;
import java.util.UUID;

/** Spring Data interface projection for a per-fund debit-minus-credit aggregate — see
 *  JournalLineRepository#sumByFundAndAccountType. */
public interface FundAmount {
    UUID getFundId();
    BigDecimal getAmount();
}

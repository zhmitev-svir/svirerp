package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {

    List<JournalLine> findByJournalEntryId(UUID journalEntryId);

    Page<JournalLine> findByAccountId(UUID accountId, Pageable pageable);

    /** Sums debits minus credits for an account — used for account balance calculations. */
    @Query("SELECT COALESCE(SUM(l.debitAmount), 0) - COALESCE(SUM(l.creditAmount), 0) " +
           "FROM JournalLine l WHERE l.account.id = :accountId AND l.journalEntry.status = 'posted'")
    BigDecimal netBalanceForAccount(UUID accountId);
}

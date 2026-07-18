package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the controller layer
    // serializes the response, so lazy associations must be eagerly fetched here.
    @EntityGraph(attributePaths = {"journalEntry", "journalEntry.org", "journalEntry.createdBy",
            "journalEntry.approvedBy", "journalEntry.payer", "journalEntry.vendor",
            "journalEntry.serviceRequest", "journalEntry.categoryAccount", "journalEntry.fund",
            "account", "account.parentAccount", "fund"})
    @Override
    Optional<JournalLine> findById(UUID id);

    @EntityGraph(attributePaths = {"journalEntry", "journalEntry.org", "journalEntry.createdBy",
            "journalEntry.approvedBy", "journalEntry.payer", "journalEntry.vendor",
            "journalEntry.serviceRequest", "journalEntry.categoryAccount", "journalEntry.fund",
            "account", "account.parentAccount", "fund"})
    List<JournalLine> findByJournalEntryId(UUID journalEntryId);

    @EntityGraph(attributePaths = {"journalEntry", "journalEntry.org", "journalEntry.createdBy",
            "journalEntry.approvedBy", "journalEntry.payer", "journalEntry.vendor",
            "journalEntry.serviceRequest", "journalEntry.categoryAccount", "journalEntry.fund",
            "account", "account.parentAccount", "fund"})
    Page<JournalLine> findByAccountId(UUID accountId, Pageable pageable);

    /** Sums debits minus credits for an account — used for account balance calculations. */
    @Query("SELECT COALESCE(SUM(l.debitAmount), 0) - COALESCE(SUM(l.creditAmount), 0) " +
           "FROM JournalLine l WHERE l.account.id = :accountId AND l.journalEntry.status = 'posted'")
    BigDecimal netBalanceForAccount(UUID accountId);

    /**
     * Sums debits minus credits for a fund, restricted to accounts of one accountType (revenue or
     * expense) — used to compute a project/fund's total income or total expense.
     */
    @Query("SELECT COALESCE(SUM(l.debitAmount), 0) - COALESCE(SUM(l.creditAmount), 0) " +
           "FROM JournalLine l WHERE l.fund.id = :fundId AND l.account.accountType = :accountType " +
           "AND l.journalEntry.status = 'posted'")
    BigDecimal netAmountForFundAndAccountType(UUID fundId, String accountType);
}

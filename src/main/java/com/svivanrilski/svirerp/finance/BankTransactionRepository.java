package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the controller layer
    // serializes the response, so lazy associations must be eagerly fetched here.
    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.glAccount", "journalEntry"})
    @Override
    Optional<BankTransaction> findById(UUID id);

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.glAccount", "journalEntry"})
    Page<BankTransaction> findByBankAccountId(UUID bankAccountId, Pageable pageable);

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.glAccount", "journalEntry"})
    Page<BankTransaction> findByBankAccountIdAndIsReconciled(UUID bankAccountId, boolean isReconciled,
            Pageable pageable);

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.glAccount", "journalEntry"})
    Page<BankTransaction> findByBankAccountIdAndTransactionDateBetween(UUID bankAccountId,
            LocalDate from, LocalDate to, Pageable pageable);
}

package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the controller layer
    // serializes the response, so lazy associations must be eagerly fetched here.
    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.glAccount", "reconciledBy"})
    @Override
    Optional<BankReconciliation> findById(UUID id);

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.glAccount", "reconciledBy"})
    Page<BankReconciliation> findByBankAccountId(UUID bankAccountId, Pageable pageable);

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.glAccount", "reconciledBy"})
    Page<BankReconciliation> findByBankAccountIdAndStatus(UUID bankAccountId, String status,
            Pageable pageable);
}

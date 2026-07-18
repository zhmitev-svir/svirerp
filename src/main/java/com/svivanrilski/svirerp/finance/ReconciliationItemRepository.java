package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the controller layer
    // serializes the response, so lazy associations must be eagerly fetched here.
    @EntityGraph(attributePaths = {"reconciliation", "reconciliation.bankAccount", "bankTransaction",
            "bankTransaction.bankAccount"})
    @Override
    Optional<ReconciliationItem> findById(UUID id);

    @EntityGraph(attributePaths = {"reconciliation", "reconciliation.bankAccount", "bankTransaction",
            "bankTransaction.bankAccount"})
    Page<ReconciliationItem> findByReconciliationId(UUID reconciliationId, Pageable pageable);

    boolean existsByReconciliationIdAndBankTransactionId(UUID reconciliationId, UUID bankTransactionId);
}

package org.svir.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, UUID> {

    Page<ReconciliationItem> findByReconciliationId(UUID reconciliationId, Pageable pageable);

    boolean existsByReconciliationIdAndBankTransactionId(UUID reconciliationId, UUID bankTransactionId);
}

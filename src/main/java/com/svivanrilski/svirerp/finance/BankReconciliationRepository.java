package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, UUID> {

    Page<BankReconciliation> findByBankAccountId(UUID bankAccountId, Pageable pageable);

    Page<BankReconciliation> findByBankAccountIdAndStatus(UUID bankAccountId, String status,
            Pageable pageable);
}

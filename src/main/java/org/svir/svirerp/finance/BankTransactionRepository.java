package org.svir.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {

    Page<BankTransaction> findByBankAccountId(UUID bankAccountId, Pageable pageable);

    Page<BankTransaction> findByBankAccountIdAndIsReconciled(UUID bankAccountId, boolean isReconciled,
            Pageable pageable);

    Page<BankTransaction> findByBankAccountIdAndTransactionDateBetween(UUID bankAccountId,
            LocalDate from, LocalDate to, Pageable pageable);
}

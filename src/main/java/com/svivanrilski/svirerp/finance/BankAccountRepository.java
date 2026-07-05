package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    Page<BankAccount> findByOrgId(UUID orgId, Pageable pageable);

    Page<BankAccount> findByOrgIdAndIsActive(UUID orgId, boolean isActive, Pageable pageable);
}

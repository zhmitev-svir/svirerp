package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Page<Account> findByOrgId(UUID orgId, Pageable pageable);

    Page<Account> findByOrgIdAndAccountType(UUID orgId, String accountType, Pageable pageable);

    /** Returns root accounts (no parent) for a given org — used to build the account tree. */
    List<Account> findByOrgIdAndParentAccountIsNull(UUID orgId);

    boolean existsByOrgIdAndAccountNumber(UUID orgId, String accountNumber);
}

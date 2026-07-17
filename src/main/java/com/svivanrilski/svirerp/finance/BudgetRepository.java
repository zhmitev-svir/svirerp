package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Page<Budget> findByOrgId(UUID orgId, Pageable pageable);

    Page<Budget> findByOrgIdAndFiscalYear(UUID orgId, int fiscalYear, Pageable pageable);
}

package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the controller layer
    // serializes the response, so lazy associations must be eagerly fetched here.
    @EntityGraph(attributePaths = {"org", "account", "account.parentAccount", "fund"})
    @Override
    Optional<Budget> findById(UUID id);

    @EntityGraph(attributePaths = {"org", "account", "account.parentAccount", "fund"})
    Page<Budget> findByOrgId(UUID orgId, Pageable pageable);

    @EntityGraph(attributePaths = {"org", "account", "account.parentAccount", "fund"})
    Page<Budget> findByOrgIdAndFiscalYear(UUID orgId, int fiscalYear, Pageable pageable);
}

package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the controller layer
    // serializes the response, so lazy associations must be eagerly fetched here.
    @EntityGraph(attributePaths = {"org", "createdBy", "approvedBy", "payer", "vendor", "serviceRequest",
            "serviceRequest.requestorPerson", "categoryAccount", "categoryAccount.parentAccount", "fund"})
    @Override
    Optional<JournalEntry> findById(UUID id);

    @EntityGraph(attributePaths = {"org", "createdBy", "approvedBy", "payer", "vendor", "serviceRequest",
            "serviceRequest.requestorPerson", "categoryAccount", "categoryAccount.parentAccount", "fund"})
    Page<JournalEntry> findByOrgId(UUID orgId, Pageable pageable);

    @EntityGraph(attributePaths = {"org", "createdBy", "approvedBy", "payer", "vendor", "serviceRequest",
            "serviceRequest.requestorPerson", "categoryAccount", "categoryAccount.parentAccount", "fund"})
    Page<JournalEntry> findByOrgIdAndStatus(UUID orgId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"org", "createdBy", "approvedBy", "payer", "vendor", "serviceRequest",
            "serviceRequest.requestorPerson", "categoryAccount", "categoryAccount.parentAccount", "fund"})
    Page<JournalEntry> findByOrgIdAndEntryDateBetween(UUID orgId, LocalDate from, LocalDate to, Pageable pageable);

    @EntityGraph(attributePaths = {"org", "createdBy", "approvedBy", "payer", "vendor", "serviceRequest",
            "serviceRequest.requestorPerson", "categoryAccount", "categoryAccount.parentAccount", "fund"})
    Page<JournalEntry> findByOrgIdAndFundId(UUID orgId, UUID fundId, Pageable pageable);

    @EntityGraph(attributePaths = {"org", "createdBy", "approvedBy", "payer", "vendor", "serviceRequest",
            "serviceRequest.requestorPerson", "categoryAccount", "categoryAccount.parentAccount", "fund"})
    Page<JournalEntry> findByOrgIdAndFundIdAndEntryDateBetween(UUID orgId, UUID fundId, LocalDate from, LocalDate to,
            Pageable pageable);

    /** Payment history for a service request — used to compute the balance still owed. */
    @EntityGraph(attributePaths = {"org", "payer", "categoryAccount", "fund"})
    List<JournalEntry> findByServiceRequestId(UUID serviceRequestId);

    /** Sums totalDebit for a service request's posted income entries — the amount paid so far. */
    @Query("SELECT COALESCE(SUM(e.totalDebit), 0) FROM JournalEntry e "
            + "WHERE e.serviceRequest.id = :serviceRequestId AND e.status = 'posted'")
    BigDecimal sumPaidForServiceRequest(UUID serviceRequestId);
}

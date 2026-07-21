package com.svivanrilski.svirerp.zeffyimport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ZeffyImportRowRepository extends JpaRepository<ZeffyImportRow, UUID> {

    // A graphed relation's own lazy fields need their own dotted paths too — journalEntry has
    // several lazy @ManyToOne fields of its own (org/createdBy/approvedBy/payer/vendor/
    // serviceRequest/categoryAccount/fund); listing only "journalEntry" leaves those as
    // uninitialized proxies that blow up at serialization time ("no session").
    @EntityGraph(attributePaths = {
        "batch", "batch.org", "org",
        "person",
        "member", "member.person", "member.org", "member.membershipType",
        "fund",
        "memberPayment", "memberPayment.member",
        "journalEntry", "journalEntry.org", "journalEntry.createdBy", "journalEntry.approvedBy",
        "journalEntry.payer", "journalEntry.vendor", "journalEntry.serviceRequest",
        "journalEntry.categoryAccount", "journalEntry.fund",
    })
    Page<ZeffyImportRow> findByBatchIdOrderByRowNumber(UUID batchId, Pageable pageable);

    /** Internal use only (row.getOutcome()/getAmount()/etc.) inside the commit transaction — never serialized. */
    List<ZeffyImportRow> findByBatchIdOrderByRowNumber(UUID batchId);

    boolean existsByOrgIdAndDedupeKeyAndOutcome(UUID orgId, String dedupeKey, String outcome);
}

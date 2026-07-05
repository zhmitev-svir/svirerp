package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    Page<JournalEntry> findByOrgId(UUID orgId, Pageable pageable);

    Page<JournalEntry> findByOrgIdAndStatus(UUID orgId, String status, Pageable pageable);

    Page<JournalEntry> findByOrgIdAndEntryDateBetween(UUID orgId, LocalDate from, LocalDate to, Pageable pageable);
}

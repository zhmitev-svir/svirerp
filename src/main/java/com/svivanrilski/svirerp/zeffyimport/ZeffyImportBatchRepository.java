package com.svivanrilski.svirerp.zeffyimport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZeffyImportBatchRepository extends JpaRepository<ZeffyImportBatch, UUID> {

    @EntityGraph(attributePaths = {"org"})
    @Override
    Optional<ZeffyImportBatch> findById(UUID id);

    @EntityGraph(attributePaths = {"org"})
    Page<ZeffyImportBatch> findByOrgIdOrderByUploadedAtDesc(UUID orgId, Pageable pageable);
}

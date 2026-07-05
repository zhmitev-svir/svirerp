package org.svir.svirerp.governance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TrusteeDocumentRepository extends JpaRepository<TrusteeDocument, UUID> {

    Page<TrusteeDocument> findByTrusteeId(UUID trusteeId, Pageable pageable);
}

package com.svivanrilski.svirerp.governance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommitteeRepository extends JpaRepository<Committee, UUID> {

    Page<Committee> findByOrgId(UUID orgId, Pageable pageable);

    Page<Committee> findByOrgIdAndIsActive(UUID orgId, boolean isActive, Pageable pageable);
}

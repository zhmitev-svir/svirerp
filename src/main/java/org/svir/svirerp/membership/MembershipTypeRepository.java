package org.svir.svirerp.membership;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MembershipTypeRepository extends JpaRepository<MembershipType, UUID> {

    Page<MembershipType> findByOrgId(UUID orgId, Pageable pageable);

    Page<MembershipType> findByOrgIdAndIsActive(UUID orgId, boolean isActive, Pageable pageable);
}

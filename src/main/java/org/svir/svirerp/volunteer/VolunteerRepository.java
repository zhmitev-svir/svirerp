package org.svir.svirerp.volunteer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, UUID> {

    Page<Volunteer> findByOrgId(UUID orgId, Pageable pageable);

    Page<Volunteer> findByOrgIdAndIsActive(UUID orgId, boolean isActive, Pageable pageable);

    boolean existsByPersonIdAndOrgId(UUID personId, UUID orgId);
}

package com.svivanrilski.svirerp.volunteer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VolunteerAreaRepository extends JpaRepository<VolunteerArea, UUID> {

    @EntityGraph(attributePaths = "org")
    Page<VolunteerArea> findByOrgId(UUID orgId, Pageable pageable);

    @EntityGraph(attributePaths = "org")
    List<VolunteerArea> findByOrgIdAndIsActiveTrue(UUID orgId);

    Optional<VolunteerArea> findByOrgIdAndNameIgnoreCase(UUID orgId, String name);

    boolean existsByOrgIdAndNameIgnoreCase(UUID orgId, String name);
}

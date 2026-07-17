package com.svivanrilski.svirerp.volunteer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, UUID> {

    // Attribute paths for {@code person}/{@code org}/{@code contactPerson}/{@code areas} are eagerly
    // fetched here because spring.jpa.open-in-view=false closes the Hibernate session before this
    // codebase's controllers (which serialize entities directly, with no DTO layer) can lazily touch
    // them — omitting this throws LazyInitializationException at request time, not at compile time.

    @Override
    @EntityGraph(attributePaths = {"person", "org", "contactPerson", "areas"})
    Optional<Volunteer> findById(UUID id);

    @EntityGraph(attributePaths = {"person", "org", "contactPerson", "areas"})
    Page<Volunteer> findByOrgId(UUID orgId, Pageable pageable);

    @EntityGraph(attributePaths = {"person", "org", "contactPerson", "areas"})
    Page<Volunteer> findByOrgIdAndIsActive(UUID orgId, boolean isActive, Pageable pageable);

    boolean existsByPersonIdAndOrgId(UUID personId, UUID orgId);
}

package org.svir.svirerp.membership;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipTypeRepository extends JpaRepository<MembershipType, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the
    // controller layer serializes the response, so the LAZY `org` association
    // must be eagerly fetched here or Jackson hits a LazyInitializationException.
    @EntityGraph(attributePaths = "org")
    @Override
    Optional<MembershipType> findById(UUID id);

    @EntityGraph(attributePaths = "org")
    Page<MembershipType> findByOrgId(UUID orgId, Pageable pageable);

    Page<MembershipType> findByOrgIdAndIsActive(UUID orgId, boolean isActive, Pageable pageable);
}

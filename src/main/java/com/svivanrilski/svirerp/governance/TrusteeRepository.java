package com.svivanrilski.svirerp.governance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrusteeRepository extends JpaRepository<Trustee, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the
    // controller layer serializes the response, so these LAZY associations
    // must be eagerly fetched here or Jackson hits a LazyInitializationException.
    @EntityGraph(attributePaths = {"person", "org"})
    @Override
    Optional<Trustee> findById(UUID id);

    @EntityGraph(attributePaths = {"person", "org"})
    Page<Trustee> findByOrgId(UUID orgId, Pageable pageable);

    Page<Trustee> findByOrgIdAndIsActive(UUID orgId, boolean isActive, Pageable pageable);
}

package com.svivanrilski.svirerp.governance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingMinutesRepository extends JpaRepository<MeetingMinutes, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the
    // controller layer serializes the response, so the lazy `org` association
    // must be eagerly fetched here or Jackson hits a LazyInitializationException.
    @EntityGraph(attributePaths = {"org"})
    @Override
    Optional<MeetingMinutes> findById(UUID id);

    @EntityGraph(attributePaths = {"org"})
    Page<MeetingMinutes> findByOrgId(UUID orgId, Pageable pageable);
}

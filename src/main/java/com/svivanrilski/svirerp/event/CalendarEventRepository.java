package com.svivanrilski.svirerp.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the
    // controller layer serializes the response, so the lazy `org`/`createdBy`
    // associations must be eagerly fetched here or Jackson hits a
    // LazyInitializationException.
    @EntityGraph(attributePaths = {"org", "createdBy"})
    @Override
    Optional<CalendarEvent> findById(UUID id);

    @EntityGraph(attributePaths = {"org", "createdBy"})
    Page<CalendarEvent> findByOrgId(UUID orgId, Pageable pageable);

    @EntityGraph(attributePaths = {"org", "createdBy"})
    Page<CalendarEvent> findByOrgIdAndStatus(UUID orgId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"org", "createdBy"})
    Page<CalendarEvent> findByOrgIdAndStartDatetimeBetween(UUID orgId, OffsetDateTime from,
            OffsetDateTime to, Pageable pageable);
}

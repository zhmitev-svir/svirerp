package org.svir.svirerp.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventResourceRepository extends JpaRepository<EventResource, UUID> {

    Page<EventResource> findByEventId(UUID eventId, Pageable pageable);
}

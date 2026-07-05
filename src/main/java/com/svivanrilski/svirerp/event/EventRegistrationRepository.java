package org.svir.svirerp.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {

    Page<EventRegistration> findByEventId(UUID eventId, Pageable pageable);

    Page<EventRegistration> findByPersonId(UUID personId, Pageable pageable);

    boolean existsByEventIdAndPersonId(UUID eventId, UUID personId);
}

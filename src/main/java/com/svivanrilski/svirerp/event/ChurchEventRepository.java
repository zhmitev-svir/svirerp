package org.svir.svirerp.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChurchEventRepository extends JpaRepository<ChurchEvent, UUID> {

    Optional<ChurchEvent> findByCalendarEventId(UUID calendarEventId);

    boolean existsByCalendarEventId(UUID calendarEventId);
}

package org.svir.svirerp.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    Page<CalendarEvent> findByOrgId(UUID orgId, Pageable pageable);

    Page<CalendarEvent> findByOrgIdAndStatus(UUID orgId, String status, Pageable pageable);

    Page<CalendarEvent> findByOrgIdAndStartDatetimeBetween(UUID orgId, OffsetDateTime from,
            OffsetDateTime to, Pageable pageable);
}

package org.svir.svirerp.volunteer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface VolunteerHourRepository extends JpaRepository<VolunteerHour, UUID> {

    Page<VolunteerHour> findByVolunteerId(UUID volunteerId, Pageable pageable);

    Page<VolunteerHour> findByVolunteerIdAndStatus(UUID volunteerId, String status, Pageable pageable);

    /** Total approved hours for a given volunteer — used for recognition reports. */
    @Query("SELECT COALESCE(SUM(h.hours), 0) FROM VolunteerHour h WHERE h.volunteer.id = :volunteerId AND h.status = 'approved'")
    BigDecimal sumApprovedHoursByVolunteer(UUID volunteerId);
}

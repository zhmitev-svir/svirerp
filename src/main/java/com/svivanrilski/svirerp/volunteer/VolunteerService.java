package org.svir.svirerp.volunteer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.svir.svirerp.common.ResourceNotFoundException;
import org.svir.svirerp.event.CalendarEvent;
import org.svir.svirerp.event.CalendarEventRepository;
import org.svir.svirerp.organization.Organization;
import org.svir.svirerp.organization.OrganizationService;
import org.svir.svirerp.person.Person;
import org.svir.svirerp.person.PersonService;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VolunteerService {

    private static final Set<String> HOUR_STATUSES = Set.of("pending", "approved", "rejected");

    private final VolunteerRepository volunteerRepo;
    private final VolunteerHourRepository hourRepo;
    private final OrganizationService orgService;
    private final PersonService personService;
    private final CalendarEventRepository calendarEventRepo;

    // ── Volunteer ─────────────────────────────────────────────────────────────

    public Page<Volunteer> findByOrg(UUID orgId, Pageable pageable) {
        return volunteerRepo.findByOrgId(orgId, pageable);
    }

    public Volunteer findById(UUID id) {
        return volunteerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer", id));
    }

    @Transactional
    public Volunteer create(Volunteer volunteer) {
        Person person = personService.findById(volunteer.getPerson().getId());
        Organization org = orgService.findById(volunteer.getOrg().getId());
        volunteer.setPerson(person);
        volunteer.setOrg(org);
        return volunteerRepo.save(volunteer);
    }

    @Transactional
    public Volunteer update(UUID id, Volunteer patch) {
        Volunteer existing = findById(id);
        existing.setOnboardDate(patch.getOnboardDate());
        existing.setIsActive(patch.getIsActive());
        existing.setSkills(patch.getSkills());
        existing.setAvailability(patch.getAvailability());
        existing.setNotes(patch.getNotes());
        return volunteerRepo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        if (!volunteerRepo.existsById(id)) throw new ResourceNotFoundException("Volunteer", id);
        volunteerRepo.deleteById(id);
    }

    // ── VolunteerHour ─────────────────────────────────────────────────────────

    public Page<VolunteerHour> findHoursByVolunteer(UUID volunteerId, Pageable pageable) {
        return hourRepo.findByVolunteerId(volunteerId, pageable);
    }

    public VolunteerHour findHourById(UUID id) {
        return hourRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VolunteerHour", id));
    }

    /** Returns total approved hours for a volunteer — for recognition/grant reporting. */
    public BigDecimal totalApprovedHours(UUID volunteerId) {
        return hourRepo.sumApprovedHoursByVolunteer(volunteerId);
    }

    @Transactional
    public VolunteerHour logHours(VolunteerHour hour) {
        validateStatus(hour.getStatus());
        Volunteer volunteer = findById(hour.getVolunteer().getId());
        hour.setVolunteer(volunteer);
        if (hour.getEvent() != null) {
            CalendarEvent event = calendarEventRepo.findById(hour.getEvent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", hour.getEvent().getId()));
            hour.setEvent(event);
        }
        return hourRepo.save(hour);
    }

    @Transactional
    public VolunteerHour updateHour(UUID id, VolunteerHour patch) {
        validateStatus(patch.getStatus());
        VolunteerHour existing = findHourById(id);
        existing.setLogDate(patch.getLogDate());
        existing.setHours(patch.getHours());
        existing.setActivityDescription(patch.getActivityDescription());
        existing.setApprovedBy(patch.getApprovedBy());
        existing.setStatus(patch.getStatus());
        if (patch.getEvent() != null) {
            CalendarEvent event = calendarEventRepo.findById(patch.getEvent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", patch.getEvent().getId()));
            existing.setEvent(event);
        } else {
            existing.setEvent(null);
        }
        return hourRepo.save(existing);
    }

    @Transactional
    public void deleteHour(UUID id) {
        if (!hourRepo.existsById(id)) throw new ResourceNotFoundException("VolunteerHour", id);
        hourRepo.deleteById(id);
    }

    private void validateStatus(String status) {
        if (status != null && !HOUR_STATUSES.contains(status))
            throw new IllegalArgumentException("Invalid hour status: " + status + ". Allowed: " + HOUR_STATUSES);
    }
}

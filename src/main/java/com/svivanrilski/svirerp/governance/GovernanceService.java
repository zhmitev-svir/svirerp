package com.svivanrilski.svirerp.governance;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.svivanrilski.svirerp.common.ResourceNotFoundException;
import com.svivanrilski.svirerp.organization.Organization;
import com.svivanrilski.svirerp.organization.OrganizationService;
import com.svivanrilski.svirerp.person.Person;
import com.svivanrilski.svirerp.person.PersonService;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GovernanceService {

    // Default term length used when renewing a lapsed trustee term.
    private static final int TRUSTEE_TERM_YEARS = 2;

    private static final Set<String> MEETING_TYPES = Set.of("regular", "special", "emergency", "annual");
    private static final Set<String> MEETING_STATUSES = Set.of("scheduled", "completed", "cancelled", "postponed");
    private static final Set<String> RESOLUTION_STATUSES = Set.of("passed", "failed", "tabled", "withdrawn");

    private final TrusteeRepository trusteeRepo;
    private final TrusteeDocumentRepository trusteeDocRepo;
    private final CommitteeRepository committeeRepo;
    private final CommitteeMemberRepository committeeMemberRepo;
    private final CommitteeMeetingRepository meetingRepo;
    private final CommitteeResolutionRepository resolutionRepo;
    private final OrganizationService orgService;
    private final PersonService personService;

    // ── Trustee ──────────────────────────────────────────────────────────────

    public Page<Trustee> findTrusteesByOrg(UUID orgId, Pageable pageable) {
        return trusteeRepo.findByOrgId(orgId, pageable);
    }

    public Trustee findTrusteeById(UUID id) {
        return trusteeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trustee", id));
    }

    @Transactional
    public Trustee createTrustee(Trustee trustee) {
        Person person = personService.findById(trustee.getPerson().getId());
        Organization org = orgService.findById(trustee.getOrg().getId());
        trustee.setPerson(person);
        trustee.setOrg(org);
        return trusteeRepo.save(trustee);
    }

    @Transactional
    public Trustee updateTrustee(UUID id, Trustee patch) {
        Trustee existing = findTrusteeById(id);
        existing.setTitle(patch.getTitle());
        existing.setRole(patch.getRole());
        existing.setTermStart(patch.getTermStart());
        existing.setTermEnd(patch.getTermEnd());
        existing.setIsActive(patch.getIsActive());
        existing.setIsOfficer(patch.getIsOfficer());
        existing.setNotes(patch.getNotes());
        return trusteeRepo.save(existing);
    }

    @Transactional
    public void deleteTrustee(UUID id) {
        if (!trusteeRepo.existsById(id)) throw new ResourceNotFoundException("Trustee", id);
        trusteeRepo.deleteById(id);
    }

    /**
     * Re-elects the same trustee for a fresh term, rather than letting
     * term_end silently drift forward — a lapsed term auto-renews in the
     * sense that the trustee keeps serving, but bumping the dates is always
     * an explicit action tied to an actual election.
     */
    @Transactional
    public Trustee renewTrustee(UUID id) {
        Trustee existing = findTrusteeById(id);
        LocalDate today = LocalDate.now();
        existing.setTermStart(today);
        existing.setTermEnd(today.plusYears(TRUSTEE_TERM_YEARS));
        return trusteeRepo.save(existing);
    }

    // ── TrusteeDocument ──────────────────────────────────────────────────────

    public Page<TrusteeDocument> findDocumentsByTrustee(UUID trusteeId, Pageable pageable) {
        return trusteeDocRepo.findByTrusteeId(trusteeId, pageable);
    }

    public TrusteeDocument findDocumentById(UUID id) {
        return trusteeDocRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrusteeDocument", id));
    }

    @Transactional
    public TrusteeDocument createDocument(TrusteeDocument doc) {
        Trustee trustee = findTrusteeById(doc.getTrustee().getId());
        doc.setTrustee(trustee);
        return trusteeDocRepo.save(doc);
    }

    @Transactional
    public TrusteeDocument updateDocument(UUID id, TrusteeDocument patch) {
        TrusteeDocument existing = findDocumentById(id);
        existing.setDocumentType(patch.getDocumentType());
        existing.setFileUrl(patch.getFileUrl());
        existing.setUploadedAt(patch.getUploadedAt());
        existing.setNotes(patch.getNotes());
        return trusteeDocRepo.save(existing);
    }

    @Transactional
    public void deleteDocument(UUID id) {
        if (!trusteeDocRepo.existsById(id)) throw new ResourceNotFoundException("TrusteeDocument", id);
        trusteeDocRepo.deleteById(id);
    }

    // ── Committee ─────────────────────────────────────────────────────────────

    public Page<Committee> findCommitteesByOrg(UUID orgId, Pageable pageable) {
        return committeeRepo.findByOrgId(orgId, pageable);
    }

    public Committee findCommitteeById(UUID id) {
        return committeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Committee", id));
    }

    @Transactional
    public Committee createCommittee(Committee committee) {
        Organization org = orgService.findById(committee.getOrg().getId());
        committee.setOrg(org);
        return committeeRepo.save(committee);
    }

    @Transactional
    public Committee updateCommittee(UUID id, Committee patch) {
        Committee existing = findCommitteeById(id);
        existing.setName(patch.getName());
        existing.setDescription(patch.getDescription());
        existing.setCommitteeType(patch.getCommitteeType());
        existing.setIsStanding(patch.getIsStanding());
        existing.setIsActive(patch.getIsActive());
        existing.setFormedDate(patch.getFormedDate());
        existing.setDissolvedDate(patch.getDissolvedDate());
        existing.setMeetingSchedule(patch.getMeetingSchedule());
        existing.setMandate(patch.getMandate());
        return committeeRepo.save(existing);
    }

    @Transactional
    public void deleteCommittee(UUID id) {
        if (!committeeRepo.existsById(id)) throw new ResourceNotFoundException("Committee", id);
        committeeRepo.deleteById(id);
    }

    // ── CommitteeMember ───────────────────────────────────────────────────────

    public Page<CommitteeMember> findMembersByCommittee(UUID committeeId, Pageable pageable) {
        return committeeMemberRepo.findByCommitteeId(committeeId, pageable);
    }

    public CommitteeMember findCommitteeMemberById(UUID id) {
        return committeeMemberRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommitteeMember", id));
    }

    @Transactional
    public CommitteeMember createCommitteeMember(CommitteeMember cm) {
        Committee committee = findCommitteeById(cm.getCommittee().getId());
        Person person = personService.findById(cm.getPerson().getId());
        cm.setCommittee(committee);
        cm.setPerson(person);
        return committeeMemberRepo.save(cm);
    }

    @Transactional
    public CommitteeMember updateCommitteeMember(UUID id, CommitteeMember patch) {
        CommitteeMember existing = findCommitteeMemberById(id);
        existing.setRole(patch.getRole());
        existing.setStartDate(patch.getStartDate());
        existing.setEndDate(patch.getEndDate());
        existing.setIsActive(patch.getIsActive());
        existing.setIsChair(patch.getIsChair());
        existing.setNotes(patch.getNotes());
        return committeeMemberRepo.save(existing);
    }

    @Transactional
    public void deleteCommitteeMember(UUID id) {
        if (!committeeMemberRepo.existsById(id)) throw new ResourceNotFoundException("CommitteeMember", id);
        committeeMemberRepo.deleteById(id);
    }

    // ── CommitteeMeeting ─────────────────────────────────────────────────────

    public Page<CommitteeMeeting> findMeetingsByCommittee(UUID committeeId, Pageable pageable) {
        return meetingRepo.findByCommitteeId(committeeId, pageable);
    }

    public CommitteeMeeting findMeetingById(UUID id) {
        return meetingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommitteeMeeting", id));
    }

    @Transactional
    public CommitteeMeeting createMeeting(CommitteeMeeting meeting) {
        validateMeetingType(meeting.getMeetingType());
        validateMeetingStatus(meeting.getStatus());
        Committee committee = findCommitteeById(meeting.getCommittee().getId());
        meeting.setCommittee(committee);
        return meetingRepo.save(meeting);
    }

    @Transactional
    public CommitteeMeeting updateMeeting(UUID id, CommitteeMeeting patch) {
        validateMeetingType(patch.getMeetingType());
        validateMeetingStatus(patch.getStatus());
        CommitteeMeeting existing = findMeetingById(id);
        existing.setMeetingDatetime(patch.getMeetingDatetime());
        existing.setLocation(patch.getLocation());
        existing.setMeetingType(patch.getMeetingType());
        existing.setAgendaUrl(patch.getAgendaUrl());
        existing.setMinutesUrl(patch.getMinutesUrl());
        existing.setStatus(patch.getStatus());
        existing.setNotes(patch.getNotes());
        return meetingRepo.save(existing);
    }

    @Transactional
    public void deleteMeeting(UUID id) {
        if (!meetingRepo.existsById(id)) throw new ResourceNotFoundException("CommitteeMeeting", id);
        meetingRepo.deleteById(id);
    }

    // ── CommitteeResolution ───────────────────────────────────────────────────

    public Page<CommitteeResolution> findResolutionsByCommittee(UUID committeeId, Pageable pageable) {
        return resolutionRepo.findByCommitteeId(committeeId, pageable);
    }

    public CommitteeResolution findResolutionById(UUID id) {
        return resolutionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommitteeResolution", id));
    }

    @Transactional
    public CommitteeResolution createResolution(CommitteeResolution resolution) {
        validateResolutionStatus(resolution.getStatus());
        Committee committee = findCommitteeById(resolution.getCommittee().getId());
        CommitteeMeeting meeting = findMeetingById(resolution.getMeeting().getId());
        resolution.setCommittee(committee);
        resolution.setMeeting(meeting);
        return resolutionRepo.save(resolution);
    }

    @Transactional
    public CommitteeResolution updateResolution(UUID id, CommitteeResolution patch) {
        validateResolutionStatus(patch.getStatus());
        CommitteeResolution existing = findResolutionById(id);
        existing.setResolutionNumber(patch.getResolutionNumber());
        existing.setTitle(patch.getTitle());
        existing.setDescription(patch.getDescription());
        existing.setStatus(patch.getStatus());
        existing.setPassedDate(patch.getPassedDate());
        existing.setVotesFor(patch.getVotesFor());
        existing.setVotesAgainst(patch.getVotesAgainst());
        existing.setAbstentions(patch.getAbstentions());
        return resolutionRepo.save(existing);
    }

    @Transactional
    public void deleteResolution(UUID id) {
        if (!resolutionRepo.existsById(id)) throw new ResourceNotFoundException("CommitteeResolution", id);
        resolutionRepo.deleteById(id);
    }

    // ── Validators ────────────────────────────────────────────────────────────

    private void validateMeetingType(String type) {
        if (type != null && !MEETING_TYPES.contains(type))
            throw new IllegalArgumentException("Invalid meeting type: " + type + ". Allowed: " + MEETING_TYPES);
    }

    private void validateMeetingStatus(String status) {
        if (status != null && !MEETING_STATUSES.contains(status))
            throw new IllegalArgumentException("Invalid meeting status: " + status + ". Allowed: " + MEETING_STATUSES);
    }

    private void validateResolutionStatus(String status) {
        if (status != null && !RESOLUTION_STATUSES.contains(status))
            throw new IllegalArgumentException("Invalid resolution status: " + status + ". Allowed: " + RESOLUTION_STATUSES);
    }
}

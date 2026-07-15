package com.svivanrilski.svirerp.governance;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GovernanceController {

    private final GovernanceService service;

    // ── Trustee ──────────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/trustees")
    public Page<Trustee> listTrustees(@PathVariable UUID orgId, Pageable pageable) {
        return service.findTrusteesByOrg(orgId, pageable);
    }

    @GetMapping("/api/trustees/{id}")
    public Trustee getTrustee(@PathVariable UUID id) {
        return service.findTrusteeById(id);
    }

    @PostMapping("/api/trustees")
    public ResponseEntity<Trustee> createTrustee(@Valid @RequestBody Trustee trustee) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTrustee(trustee));
    }

    @PutMapping("/api/trustees/{id}")
    public Trustee updateTrustee(@PathVariable UUID id, @Valid @RequestBody Trustee trustee) {
        return service.updateTrustee(id, trustee);
    }

    @DeleteMapping("/api/trustees/{id}")
    public ResponseEntity<Void> deleteTrustee(@PathVariable UUID id) {
        service.deleteTrustee(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/trustees/{id}/renew")
    public Trustee renewTrustee(@PathVariable UUID id) {
        return service.renewTrustee(id);
    }

    // ── TrusteeDocument ──────────────────────────────────────────────────────

    @GetMapping("/api/trustees/{trusteeId}/documents")
    public Page<TrusteeDocument> listDocuments(@PathVariable UUID trusteeId, Pageable pageable) {
        return service.findDocumentsByTrustee(trusteeId, pageable);
    }

    @GetMapping("/api/trustee-documents/{id}")
    public TrusteeDocument getDocument(@PathVariable UUID id) {
        return service.findDocumentById(id);
    }

    @PostMapping("/api/trustee-documents")
    public ResponseEntity<TrusteeDocument> createDocument(@Valid @RequestBody TrusteeDocument doc) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDocument(doc));
    }

    @PutMapping("/api/trustee-documents/{id}")
    public TrusteeDocument updateDocument(@PathVariable UUID id, @Valid @RequestBody TrusteeDocument doc) {
        return service.updateDocument(id, doc);
    }

    @DeleteMapping("/api/trustee-documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        service.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    // ── Committee ─────────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/committees")
    public Page<Committee> listCommittees(@PathVariable UUID orgId, Pageable pageable) {
        return service.findCommitteesByOrg(orgId, pageable);
    }

    @GetMapping("/api/committees/{id}")
    public Committee getCommittee(@PathVariable UUID id) {
        return service.findCommitteeById(id);
    }

    @PostMapping("/api/committees")
    public ResponseEntity<Committee> createCommittee(@Valid @RequestBody Committee committee) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCommittee(committee));
    }

    @PutMapping("/api/committees/{id}")
    public Committee updateCommittee(@PathVariable UUID id, @Valid @RequestBody Committee committee) {
        return service.updateCommittee(id, committee);
    }

    @DeleteMapping("/api/committees/{id}")
    public ResponseEntity<Void> deleteCommittee(@PathVariable UUID id) {
        service.deleteCommittee(id);
        return ResponseEntity.noContent().build();
    }

    // ── CommitteeMember ───────────────────────────────────────────────────────

    @GetMapping("/api/committees/{committeeId}/members")
    public Page<CommitteeMember> listCommitteeMembers(@PathVariable UUID committeeId, Pageable pageable) {
        return service.findMembersByCommittee(committeeId, pageable);
    }

    @GetMapping("/api/committee-members/{id}")
    public CommitteeMember getCommitteeMember(@PathVariable UUID id) {
        return service.findCommitteeMemberById(id);
    }

    @PostMapping("/api/committee-members")
    public ResponseEntity<CommitteeMember> createCommitteeMember(@Valid @RequestBody CommitteeMember cm) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCommitteeMember(cm));
    }

    @PutMapping("/api/committee-members/{id}")
    public CommitteeMember updateCommitteeMember(@PathVariable UUID id, @Valid @RequestBody CommitteeMember cm) {
        return service.updateCommitteeMember(id, cm);
    }

    @DeleteMapping("/api/committee-members/{id}")
    public ResponseEntity<Void> deleteCommitteeMember(@PathVariable UUID id) {
        service.deleteCommitteeMember(id);
        return ResponseEntity.noContent().build();
    }

    // ── CommitteeMeeting ─────────────────────────────────────────────────────

    @GetMapping("/api/committees/{committeeId}/meetings")
    public Page<CommitteeMeeting> listMeetings(@PathVariable UUID committeeId, Pageable pageable) {
        return service.findMeetingsByCommittee(committeeId, pageable);
    }

    @GetMapping("/api/committee-meetings/{id}")
    public CommitteeMeeting getMeeting(@PathVariable UUID id) {
        return service.findMeetingById(id);
    }

    @PostMapping("/api/committee-meetings")
    public ResponseEntity<CommitteeMeeting> createMeeting(@Valid @RequestBody CommitteeMeeting meeting) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMeeting(meeting));
    }

    @PutMapping("/api/committee-meetings/{id}")
    public CommitteeMeeting updateMeeting(@PathVariable UUID id, @Valid @RequestBody CommitteeMeeting meeting) {
        return service.updateMeeting(id, meeting);
    }

    @DeleteMapping("/api/committee-meetings/{id}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable UUID id) {
        service.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }

    // ── CommitteeResolution ───────────────────────────────────────────────────

    @GetMapping("/api/committees/{committeeId}/resolutions")
    public Page<CommitteeResolution> listResolutions(@PathVariable UUID committeeId, Pageable pageable) {
        return service.findResolutionsByCommittee(committeeId, pageable);
    }

    @GetMapping("/api/committee-resolutions/{id}")
    public CommitteeResolution getResolution(@PathVariable UUID id) {
        return service.findResolutionById(id);
    }

    @PostMapping("/api/committee-resolutions")
    public ResponseEntity<CommitteeResolution> createResolution(@Valid @RequestBody CommitteeResolution resolution) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createResolution(resolution));
    }

    @PutMapping("/api/committee-resolutions/{id}")
    public CommitteeResolution updateResolution(@PathVariable UUID id, @Valid @RequestBody CommitteeResolution resolution) {
        return service.updateResolution(id, resolution);
    }

    @DeleteMapping("/api/committee-resolutions/{id}")
    public ResponseEntity<Void> deleteResolution(@PathVariable UUID id) {
        service.deleteResolution(id);
        return ResponseEntity.noContent().build();
    }
}

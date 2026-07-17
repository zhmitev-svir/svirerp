package com.svivanrilski.svirerp.membership;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService service;
    private final MemberImportService importService;

    // ── MembershipType ──────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/membership-types")
    public Page<MembershipType> listTypes(@PathVariable UUID orgId, Pageable pageable) {
        return service.findAllTypes(orgId, pageable);
    }

    @GetMapping("/api/membership-types/{id}")
    public MembershipType getType(@PathVariable UUID id) {
        return service.findTypeById(id);
    }

    @PostMapping("/api/membership-types")
    public ResponseEntity<MembershipType> createType(@Valid @RequestBody MembershipType type) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createType(type));
    }

    @PutMapping("/api/membership-types/{id}")
    public MembershipType updateType(@PathVariable UUID id, @Valid @RequestBody MembershipType type) {
        return service.updateType(id, type);
    }

    @DeleteMapping("/api/membership-types/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable UUID id) {
        service.deleteType(id);
        return ResponseEntity.noContent().build();
    }

    // ── Member ───────────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/members")
    public Page<Member> listMembers(@PathVariable UUID orgId, Pageable pageable) {
        return service.findAllMembers(orgId, pageable);
    }

    @GetMapping("/api/organizations/{orgId}/members/import-template")
    public ResponseEntity<byte[]> importTemplate(@PathVariable UUID orgId) {
        byte[] csv = importService.buildImportTemplateCsv();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("member-import-template.csv")
                        .build()
                        .toString())
                .body(csv);
    }

    @PostMapping("/api/organizations/{orgId}/members/import")
    public MemberImportService.MemberImportResult importMembers(
            @PathVariable UUID orgId, @RequestParam("file") MultipartFile file) {
        return importService.importMembers(orgId, file);
    }

    @GetMapping("/api/members/{id}")
    public Member getMember(@PathVariable UUID id) {
        return service.findMemberById(id);
    }

    @GetMapping("/api/members/expired")
    public List<Member> expiredMembers() {
        return service.findExpiredMembers();
    }

    @PostMapping("/api/members")
    public ResponseEntity<Member> createMember(@Valid @RequestBody Member member) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMember(member));
    }

    @PutMapping("/api/members/{id}")
    public Member updateMember(@PathVariable UUID id, @Valid @RequestBody Member member) {
        return service.updateMember(id, member);
    }

    @DeleteMapping("/api/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID id) {
        service.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    // ── MemberPayment ────────────────────────────────────────────────────────

    @GetMapping("/api/members/{memberId}/payments")
    public Page<MemberPayment> listPayments(@PathVariable UUID memberId, Pageable pageable) {
        return service.findPaymentsByMember(memberId, pageable);
    }

    @GetMapping("/api/member-payments/{id}")
    public MemberPayment getPayment(@PathVariable UUID id) {
        return service.findPaymentById(id);
    }

    @PostMapping("/api/member-payments")
    public ResponseEntity<MemberPayment> createPayment(@Valid @RequestBody MemberPayment payment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(payment));
    }

    @PutMapping("/api/member-payments/{id}")
    public MemberPayment updatePayment(@PathVariable UUID id, @Valid @RequestBody MemberPayment payment) {
        return service.updatePayment(id, payment);
    }

    @DeleteMapping("/api/member-payments/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID id) {
        service.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}

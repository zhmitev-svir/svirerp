package com.svivanrilski.svirerp.membership;

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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MembershipService {

    // Valid values for each status/method column — mirrors the DB CHECK constraints.
    private static final Set<String> MEMBER_STATUSES =
            Set.of("active", "inactive", "suspended", "expired", "pending");
    private static final Set<String> PAYMENT_METHODS =
            Set.of("cash", "check", "credit_card", "ach", "online", "other");
    private static final Set<String> PAYMENT_STATUSES =
            Set.of("pending", "completed", "failed", "refunded");

    private final MembershipTypeRepository typeRepo;
    private final MemberRepository memberRepo;
    private final MemberPaymentRepository paymentRepo;
    private final OrganizationService orgService;
    private final PersonService personService;

    // ── MembershipType ──────────────────────────────────────────────────────

    public Page<MembershipType> findAllTypes(UUID orgId, Pageable pageable) {
        return typeRepo.findByOrgId(orgId, pageable);
    }

    public MembershipType findTypeById(UUID id) {
        return typeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipType", id));
    }

    @Transactional
    public MembershipType createType(MembershipType type) {
        // Ensure the org exists before saving
        Organization org = orgService.findById(type.getOrg().getId());
        type.setOrg(org);
        return typeRepo.save(type);
    }

    @Transactional
    public MembershipType updateType(UUID id, MembershipType patch) {
        MembershipType existing = findTypeById(id);
        existing.setName(patch.getName());
        existing.setDescription(patch.getDescription());
        existing.setAnnualFee(patch.getAnnualFee());
        existing.setDurationMonths(patch.getDurationMonths());
        existing.setIsActive(patch.getIsActive());
        existing.setCanVote(patch.getCanVote());
        existing.setBenefits(patch.getBenefits());
        existing.setMaxMembers(patch.getMaxMembers());
        return typeRepo.save(existing);
    }

    @Transactional
    public void deleteType(UUID id) {
        if (!typeRepo.existsById(id)) throw new ResourceNotFoundException("MembershipType", id);
        typeRepo.deleteById(id);
    }

    // ── Member ───────────────────────────────────────────────────────────────

    public Page<Member> findAllMembers(UUID orgId, String status, Pageable pageable) {
        if (status == null || status.isBlank()) {
            return memberRepo.findByOrgId(orgId, pageable);
        }
        validateMemberStatus(status);
        return memberRepo.findByOrgIdAndStatus(orgId, status, pageable);
    }

    public Member findMemberById(UUID id) {
        return memberRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    /** Returns all members whose expiry date is before today (candidates for renewal notices). */
    public List<Member> findExpiredMembers() {
        return memberRepo.findByExpiryDateBefore(LocalDate.now());
    }

    @Transactional
    public Member createMember(Member member) {
        validateMemberStatus(member.getStatus());
        Person person = personService.findById(member.getPerson().getId());
        Organization org = orgService.findById(member.getOrg().getId());
        MembershipType type = findTypeById(member.getMembershipType().getId());
        // A person may hold at most one membership per organisation — to switch
        // membership type, update the existing Member record instead of creating another.
        if (memberRepo.existsByPersonIdAndOrgId(person.getId(), org.getId())) {
            throw new IllegalArgumentException(
                    person.getFirstName() + " " + person.getLastName() + " already has a membership in this organization");
        }
        member.setPerson(person);
        member.setOrg(org);
        member.setMembershipType(type);
        return memberRepo.save(member);
    }

    @Transactional
    public Member updateMember(UUID id, Member patch) {
        validateMemberStatus(patch.getStatus());
        Member existing = findMemberById(id);
        existing.setMembershipType(findTypeById(patch.getMembershipType().getId()));
        existing.setMemberNumber(patch.getMemberNumber());
        existing.setJoinDate(patch.getJoinDate());
        existing.setExpiryDate(patch.getExpiryDate());
        existing.setStatus(patch.getStatus());
        existing.setEmailOptIn(patch.getEmailOptIn());
        return memberRepo.save(existing);
    }

    @Transactional
    public void deleteMember(UUID id) {
        if (!memberRepo.existsById(id)) throw new ResourceNotFoundException("Member", id);
        memberRepo.deleteById(id);
    }

    public enum ImportOutcome { CREATED, UPDATED }

    /**
     * Called once per CSV row by MemberImportService — deliberately a separate
     * bean-to-bean call (not a loop within this same class) so each row gets its
     * own transaction. Without that, a DB-level failure partway through a big
     * multi-row transaction can mark the whole thing rollback-only, silently
     * discarding otherwise-successful rows despite catching the exception.
     */
    @Transactional
    public ImportOutcome importOrUpdateMemberFromRow(UUID orgId, MemberImportRow row) {
        validateMemberStatus(row.status());
        Organization org = orgService.findById(orgId);

        MembershipType type = typeRepo.findByOrgIdAndNameIgnoreCase(orgId, row.membershipTypeName())
                .orElseThrow(() -> new IllegalArgumentException("Unknown membership type: " + row.membershipTypeName()));

        Person person = personService.findByEmailIfExists(row.email())
                .orElseGet(() -> personService.create(Person.builder()
                        .firstName(row.firstName())
                        .lastName(row.lastName())
                        .email(row.email())
                        .phone(row.phone())
                        .addressLine1(row.addressLine1())
                        .city(row.city())
                        .state(row.state())
                        .zip(row.zip())
                        .dateOfBirth(row.dateOfBirth())
                        .build()));

        Optional<Member> existing = memberRepo.findByPersonIdAndOrgId(person.getId(), orgId);
        if (existing.isPresent()) {
            Member member = existing.get();
            member.setMembershipType(type);
            member.setMemberNumber(row.memberNumber());
            member.setJoinDate(row.joinDate());
            member.setExpiryDate(row.expiryDate());
            member.setStatus(row.status() != null ? row.status() : member.getStatus());
            member.setEmailOptIn(row.emailOptIn() != null ? row.emailOptIn() : member.getEmailOptIn());
            memberRepo.save(member);
            return ImportOutcome.UPDATED;
        }

        Member member = Member.builder()
                .person(person)
                .org(org)
                .membershipType(type)
                .memberNumber(row.memberNumber())
                .joinDate(row.joinDate())
                .expiryDate(row.expiryDate())
                .status(row.status() != null ? row.status() : "active")
                .emailOptIn(row.emailOptIn() != null ? row.emailOptIn() : true)
                .build();
        memberRepo.save(member);
        return ImportOutcome.CREATED;
    }

    // ── MemberPayment ────────────────────────────────────────────────────────

    public Page<MemberPayment> findPaymentsByMember(UUID memberId, Pageable pageable) {
        return paymentRepo.findByMemberId(memberId, pageable);
    }

    public MemberPayment findPaymentById(UUID id) {
        return paymentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MemberPayment", id));
    }

    @Transactional
    public MemberPayment createPayment(MemberPayment payment) {
        validatePaymentMethod(payment.getPaymentMethod());
        validatePaymentStatus(payment.getStatus());
        Member member = findMemberById(payment.getMember().getId());
        payment.setMember(member);
        return paymentRepo.save(payment);
    }

    @Transactional
    public MemberPayment updatePayment(UUID id, MemberPayment patch) {
        validatePaymentMethod(patch.getPaymentMethod());
        validatePaymentStatus(patch.getStatus());
        MemberPayment existing = findPaymentById(id);
        existing.setAmount(patch.getAmount());
        existing.setPaymentDate(patch.getPaymentDate());
        existing.setPaymentMethod(patch.getPaymentMethod());
        existing.setTransactionRef(patch.getTransactionRef());
        existing.setPeriodStart(patch.getPeriodStart());
        existing.setPeriodEnd(patch.getPeriodEnd());
        existing.setStatus(patch.getStatus());
        existing.setNotes(patch.getNotes());
        return paymentRepo.save(existing);
    }

    @Transactional
    public void deletePayment(UUID id) {
        if (!paymentRepo.existsById(id)) throw new ResourceNotFoundException("MemberPayment", id);
        paymentRepo.deleteById(id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validateMemberStatus(String status) {
        if (status != null && !MEMBER_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid member status: " + status
                    + ". Allowed: " + MEMBER_STATUSES);
        }
    }

    private void validatePaymentMethod(String method) {
        if (method != null && !PAYMENT_METHODS.contains(method)) {
            throw new IllegalArgumentException("Invalid payment method: " + method
                    + ". Allowed: " + PAYMENT_METHODS);
        }
    }

    private void validatePaymentStatus(String status) {
        if (status != null && !PAYMENT_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid payment status: " + status
                    + ". Allowed: " + PAYMENT_STATUSES);
        }
    }
}

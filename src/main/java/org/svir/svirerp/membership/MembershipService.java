package org.svir.svirerp.membership;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.svir.svirerp.common.ResourceNotFoundException;
import org.svir.svirerp.organization.Organization;
import org.svir.svirerp.organization.OrganizationService;
import org.svir.svirerp.person.Person;
import org.svir.svirerp.person.PersonService;

import java.time.LocalDate;
import java.util.List;
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

    public Page<Member> findAllMembers(UUID orgId, Pageable pageable) {
        return memberRepo.findByOrgId(orgId, pageable);
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

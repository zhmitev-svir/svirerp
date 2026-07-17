package com.svivanrilski.svirerp.membership;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberPaymentRepository extends JpaRepository<MemberPayment, UUID> {

    // spring.jpa.open-in-view=false closes the Hibernate session before the
    // controller layer serializes the response, so the lazy `member` association
    // (and everything nested under it) must be eagerly fetched here or Jackson
    // hits a LazyInitializationException.
    @EntityGraph(attributePaths = {"member", "member.person", "member.org", "member.membershipType"})
    @Override
    Optional<MemberPayment> findById(UUID id);

    @EntityGraph(attributePaths = {"member", "member.person", "member.org", "member.membershipType"})
    Page<MemberPayment> findByMemberId(UUID memberId, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "member.person", "member.org", "member.membershipType"})
    Page<MemberPayment> findByMemberIdAndStatus(UUID memberId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "member.person", "member.org", "member.membershipType"})
    Page<MemberPayment> findByMemberOrgId(UUID orgId, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "member.person", "member.org", "member.membershipType"})
    Page<MemberPayment> findByMemberOrgIdAndPaymentDateGreaterThanEqual(UUID orgId, LocalDate fromDate, Pageable pageable);
}

package org.svir.svirerp.membership;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MemberPaymentRepository extends JpaRepository<MemberPayment, UUID> {

    Page<MemberPayment> findByMemberId(UUID memberId, Pageable pageable);

    Page<MemberPayment> findByMemberIdAndStatus(UUID memberId, String status, Pageable pageable);
}

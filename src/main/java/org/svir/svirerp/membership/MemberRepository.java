package org.svir.svirerp.membership;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    Page<Member> findByOrgId(UUID orgId, Pageable pageable);

    Page<Member> findByOrgIdAndStatus(UUID orgId, String status, Pageable pageable);

    List<Member> findByExpiryDateBefore(LocalDate date);

    boolean existsByPersonIdAndOrgIdAndStatus(UUID personId, UUID orgId, String status);
}

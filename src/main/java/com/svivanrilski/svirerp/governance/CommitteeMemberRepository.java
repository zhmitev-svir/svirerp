package com.svivanrilski.svirerp.governance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommitteeMemberRepository extends JpaRepository<CommitteeMember, UUID> {

    Page<CommitteeMember> findByCommitteeId(UUID committeeId, Pageable pageable);

    Page<CommitteeMember> findByPersonId(UUID personId, Pageable pageable);
}

package com.svivanrilski.svirerp.governance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommitteeMeetingRepository extends JpaRepository<CommitteeMeeting, UUID> {

    Page<CommitteeMeeting> findByCommitteeId(UUID committeeId, Pageable pageable);

    Page<CommitteeMeeting> findByCommitteeIdAndStatus(UUID committeeId, String status, Pageable pageable);
}

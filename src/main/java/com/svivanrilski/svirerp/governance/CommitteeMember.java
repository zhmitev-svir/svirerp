package com.svivanrilski.svirerp.governance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.svivanrilski.svirerp.person.Person;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Associates a Person with a Committee, preserving historical membership via date ranges.
 * A person may be re-added after a gap — use start/end dates to distinguish terms.
 */
@Entity
@Table(name = "committee_member")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id", nullable = false)
    private Committee committee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(length = 100)
    private String role;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_chair", nullable = false)
    private Boolean isChair = false;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

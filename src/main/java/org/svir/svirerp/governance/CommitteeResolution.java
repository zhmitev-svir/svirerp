package org.svir.svirerp.governance;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/** Formal resolution produced at a committee meeting with vote tallies. */
@Entity
@Table(name = "committee_resolution")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeResolution {

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
    @JoinColumn(name = "meeting_id", nullable = false)
    private CommitteeMeeting meeting;

    @Column(name = "resolution_number", unique = true, length = 50)
    private String resolutionNumber;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Allowed values (DB CHECK): passed, failed, tabled, withdrawn. */
    @Column(nullable = false, length = 30)
    private String status = "passed";

    @Column(name = "passed_date")
    private LocalDate passedDate;

    @Min(0)
    @Column(name = "votes_for", nullable = false)
    private Integer votesFor = 0;

    @Min(0)
    @Column(name = "votes_against", nullable = false)
    private Integer votesAgainst = 0;

    @Min(0)
    @Column(name = "abstentions", nullable = false)
    private Integer abstentions = 0;
}

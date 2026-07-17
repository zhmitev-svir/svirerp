package com.svivanrilski.svirerp.governance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Meeting record with agenda and minutes document links. */
@Entity
@Table(name = "committee_meeting")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id", nullable = false)
    private Committee committee;

    @NotNull
    @Column(name = "meeting_datetime", nullable = false)
    private OffsetDateTime meetingDatetime;

    @Column(length = 255)
    private String location;

    /**
     * Allowed values (DB CHECK): regular, special, emergency, annual.
     * Defaults to 'regular' to match the DB column default.
     */
    @Column(name = "meeting_type", nullable = false, length = 50)
    private String meetingType = "regular";

    @Column(name = "agenda_url", length = 500)
    private String agendaUrl;

    @Column(name = "minutes_url", length = 500)
    private String minutesUrl;

    /** Allowed values (DB CHECK): scheduled, completed, cancelled, postponed. */
    @Column(nullable = false, length = 30)
    private String status = "scheduled";

    @Column(columnDefinition = "TEXT")
    private String notes;
}

package com.svivanrilski.svirerp.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.svivanrilski.svirerp.organization.Organization;
import com.svivanrilski.svirerp.person.Person;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base event record for all organisation events.
 * Church-specific details extend this via ChurchEvent (one-to-one).
 * Volunteer hours optionally reference a calendar event.
 */
@Entity
@Table(name = "calendar_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    /** Nullable: set to NULL when the creating person record is deleted. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Person createdBy;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType = "general";

    @NotNull
    @Column(name = "start_datetime", nullable = false)
    private OffsetDateTime startDatetime;

    @Column(name = "end_datetime")
    private OffsetDateTime endDatetime;

    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay = false;

    @Column(length = 255)
    private String location;

    @Column(name = "virtual_link", length = 500)
    private String virtualLink;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @Column(name = "recurrence_rule", length = 255)
    private String recurrenceRule;

    /** Allowed values (DB CHECK): scheduled, cancelled, completed, postponed. */
    @Column(nullable = false, length = 30)
    private String status = "scheduled";

    /** Allowed values (DB CHECK): public, members_only, internal. */
    @Column(nullable = false, length = 30)
    private String visibility = "public";

    private Integer capacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}

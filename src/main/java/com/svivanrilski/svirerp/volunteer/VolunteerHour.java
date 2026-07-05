package com.svivanrilski.svirerp.volunteer;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import com.svivanrilski.svirerp.event.CalendarEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Logs service hours per volunteer per event for grant reporting and recognition.
 *
 * The event_id FK to calendar_event was deferred (added in V14 after the calendar_event table
 * existed).  From JPA's perspective this is a normal nullable @ManyToOne — migration timing
 * is transparent to the entity model.
 */
@Entity
@Table(name = "volunteer_hour")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerHour {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    /** Nullable: the FK allows null for hours logged outside any specific event. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private CalendarEvent event;

    @NotNull
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Column(name = "activity_description", columnDefinition = "TEXT")
    private String activityDescription;

    @Column(name = "approved_by", length = 255)
    private String approvedBy;

    /** Allowed values (DB CHECK): pending, approved, rejected. */
    @Column(nullable = false, length = 30)
    private String status = "pending";
}

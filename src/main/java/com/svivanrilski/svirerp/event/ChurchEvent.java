package org.svir.svirerp.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Extends CalendarEvent with worship-specific fields.
 * Uses its own UUID PK plus a unique FK to calendar_event — NOT a shared-PK one-to-one.
 * Always load together with the parent calendar event for display purposes.
 */
@Entity
@Table(name = "church_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChurchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_event_id", unique = true, nullable = false)
    private CalendarEvent calendarEvent;

    @Column(name = "service_type", length = 100)
    private String serviceType;

    @Column(name = "liturgical_season", length = 100)
    private String liturgicalSeason;

    @Column(length = 255)
    private String officiant;

    @Column(name = "sermon_title", length = 255)
    private String sermonTitle;

    @Column(name = "scripture_readings", columnDefinition = "TEXT")
    private String scriptureReadings;

    @Column(name = "music_selections", columnDefinition = "TEXT")
    private String musicSelections;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "offering_collected", precision = 10, scale = 2)
    private BigDecimal offeringCollected = BigDecimal.ZERO;

    @Column(name = "attendance_count")
    private Integer attendanceCount = 0;
}

package org.svir.svirerp.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.svir.svirerp.person.Person;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Tracks person registrations for calendar events; each person can register once per event. */
@Entity
@Table(
    name = "event_registration",
    uniqueConstraints = @UniqueConstraint(name = "uq_event_registration",
        columnNames = {"event_id", "person_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private OffsetDateTime registeredAt;

    /** Allowed values (DB CHECK): registered, attended, cancelled, waitlisted, no_show. */
    @Column(nullable = false, length = 30)
    private String status = "registered";

    @Column(name = "fee_paid", precision = 10, scale = 2)
    private BigDecimal feePaid = BigDecimal.ZERO;

    @Column(name = "ticket_number", unique = true, length = 50)
    private String ticketNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    private void prePersist() {
        if (registeredAt == null) registeredAt = OffsetDateTime.now();
    }
}

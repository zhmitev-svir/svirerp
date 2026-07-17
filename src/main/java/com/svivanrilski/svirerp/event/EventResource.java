package com.svivanrilski.svirerp.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/** Tracks resources (rooms, equipment, personnel) assigned to events. */
@Entity
@Table(name = "event_resource")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    @NotBlank
    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @NotBlank
    @Column(name = "resource_name", nullable = false, length = 255)
    private String resourceName;

    @Column(name = "assigned_to", length = 255)
    private String assignedTo;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

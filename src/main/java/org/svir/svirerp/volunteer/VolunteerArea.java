package org.svir.svirerp.volunteer;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.svir.svirerp.organization.Organization;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Org-scoped lookup of volunteer service areas (Construction, Cooking Crew, etc.). */
@Entity
@Table(
    name = "volunteer_area",
    uniqueConstraints = @UniqueConstraint(name = "uq_volunteer_area_name_org",
        columnNames = {"org_id", "name"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerArea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}

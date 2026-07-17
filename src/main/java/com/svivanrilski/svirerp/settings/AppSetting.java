package com.svivanrilski.svirerp.settings;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Generic key/value app configuration, editable at runtime via the admin-only
 * Settings page instead of requiring a restart to change a property. valueType
 * is a plain String (not a JPA enum) validated against a fixed set of allowed
 * values in the service layer, matching how status-like columns are handled
 * elsewhere in this codebase (e.g. Member.status) rather than @Enumerated.
 */
@Entity
@Table(name = "app_setting")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    @NotBlank
    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

package com.svivanrilski.svirerp.governance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Action item captured live during a meeting, optionally assigned to a trustee. */
@Entity
@Table(name = "action_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_minutes_id", nullable = false)
    private MeetingMinutes meetingMinutes;

    // Optional — an action item can be logged before anyone's assigned to it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_trustee_id")
    private Trustee assigneeTrustee;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, length = 20)
    private String priority = "normal";

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, length = 20)
    private String status = "new";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

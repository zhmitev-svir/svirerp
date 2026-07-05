package com.svivanrilski.svirerp.governance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/** Documents associated with a trustee (e.g. signed agreements, ID scans). */
@Entity
@Table(name = "trustee_document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrusteeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trustee_id", nullable = false)
    private Trustee trustee;

    @NotBlank
    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @NotBlank
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDate uploadedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    private void prePersist() {
        if (uploadedAt == null) uploadedAt = LocalDate.now();
    }
}

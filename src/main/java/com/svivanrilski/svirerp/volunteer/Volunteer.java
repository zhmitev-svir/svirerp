package com.svivanrilski.svirerp.volunteer;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.svivanrilski.svirerp.organization.Organization;
import com.svivanrilski.svirerp.person.Person;

import java.time.LocalDate;
import java.util.UUID;

/** Volunteer roster entry linking a Person to the Organisation. */
@Entity
@Table(name = "volunteer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    @NotNull
    @Column(name = "onboard_date", nullable = false)
    private LocalDate onboardDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(length = 255)
    private String availability;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

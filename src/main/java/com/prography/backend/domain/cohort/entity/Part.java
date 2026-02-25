package com.prography.backend.domain.cohort.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "parts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_parts_cohort_id_name", columnNames = {"cohort_id", "name"})
        }
)
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_parts_cohort"))
    private Cohort cohort;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Builder
    public Part(Cohort cohort, String name) {
        this.cohort = cohort;
        this.name = name;
    }
}
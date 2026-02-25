package com.prography.backend.domain.cohort.entity;

import com.prography.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "cohorts")
public class Cohort extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer generation;

    @Column(nullable = false, length = 50)
    private String name;

    protected Cohort() {}

    public Cohort(Integer generation, String name) {
        this.generation = generation;
        this.name = name;
    }

    public Long getId() { return id; }
    public Integer getGeneration() { return generation; }
    public String getName() { return name; }
}
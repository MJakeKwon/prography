package com.prography.backend.domain.cohort.dto;

import com.prography.backend.domain.cohort.entity.Cohort;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CohortResponse {
    private Long id;
    private Integer generation;
    private String name;

    public static CohortResponse from(Cohort cohort) {
        return CohortResponse.builder()
                .id(cohort.getId())
                .generation(cohort.getGeneration())
                .name(cohort.getName())
                .build();
    }
}
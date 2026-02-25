package com.prography.backend.domain.cohort.controller;

import com.prography.backend.domain.cohort.dto.CohortResponse;
import com.prography.backend.domain.cohort.service.CohortReadService;
import com.prography.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/cohorts")
public class AdminCohortController {

    private final CohortReadService cohortReadService;

    // #8 기수 목록
    @GetMapping
    public ApiResponse<List<CohortResponse>> getCohorts() {
        List<CohortResponse> result = cohortReadService.getAllCohorts()
                .stream()
                .map(CohortResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // #9 기수 상세
    @GetMapping("/{id}")
    public ApiResponse<CohortResponse> getCohort(@PathVariable Long id) {
        return ApiResponse.ok(CohortResponse.from(cohortReadService.getCohort(id)));
    }
}
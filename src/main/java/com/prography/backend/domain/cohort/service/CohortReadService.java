package com.prography.backend.domain.cohort.service;

import com.prography.backend.domain.cohort.entity.Cohort;
import com.prography.backend.domain.cohort.repository.CohortRepository;
import com.prography.backend.global.exception.BusinessException;
import com.prography.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CohortReadService {

    public static final int CURRENT_GENERATION = 11;

    private final CohortRepository cohortRepository;

    public List<Cohort> getAllCohorts() {
        return cohortRepository.findAll();
    }

    public Cohort getCohort(Long cohortId) {
        return cohortRepository.findById(cohortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));
    }

    public Cohort getCurrentCohort() {
        return cohortRepository.findByGeneration(CURRENT_GENERATION)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));
    }
}
package com.prography.backend.domain.session.dto;

import com.prography.backend.domain.session.entity.SessionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class CreateSessionRequest {

    @NotBlank
    private String title;

    @NotNull
    private LocalDate sessionDate;

    @NotNull
    private LocalTime sessionTime;

    @NotBlank
    private String location;

    private SessionStatus status;
}
package com.prography.backend.domain.session.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class UpdateSessionRequest {
    private String title;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private String location;
}
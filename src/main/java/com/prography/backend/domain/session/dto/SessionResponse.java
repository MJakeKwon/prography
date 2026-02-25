package com.prography.backend.domain.session.dto;

import com.prography.backend.domain.session.entity.Session;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class SessionResponse {
    private Long id;
    private String title;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private String location;
    private String status;

    public static SessionResponse from(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .sessionDate(session.getSessionDate())
                .sessionTime(session.getSessionTime())
                .location(session.getLocation())
                .status(session.getStatus().name())
                .build();
    }
}
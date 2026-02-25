package com.prography.backend.domain.session.dto;

import com.prography.backend.domain.session.entity.QRCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QRCodeResponse {
    private Long id;
    private Long sessionId;
    private String hashValue;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    public static QRCodeResponse from(QRCode qrCode) {
        return QRCodeResponse.builder()
                .id(qrCode.getId())
                .sessionId(qrCode.getSession().getId())
                .hashValue(qrCode.getHashValue())
                .expiresAt(qrCode.getExpiresAt())
                .revokedAt(qrCode.getRevokedAt())
                .build();
    }
}
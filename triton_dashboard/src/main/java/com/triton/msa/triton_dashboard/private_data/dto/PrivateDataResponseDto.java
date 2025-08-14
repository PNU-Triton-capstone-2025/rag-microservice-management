package com.triton.msa.triton_dashboard.private_data.dto;

import java.time.Instant;

public record PrivateDataResponseDto(
        Long id,
        Long projectId,
        String filename,
        String contentType,
        Instant createdAt
) {
    public static PrivateDataResponseDto from(ProjectPrivateDataDto dto) {
        return new PrivateDataResponseDto(
            dto.id(),
            dto.projectId(),
            dto.filename(),
            dto.contentType(),
            dto.createdAt()
        );
    }
}

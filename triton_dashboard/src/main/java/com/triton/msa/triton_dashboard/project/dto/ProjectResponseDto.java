package com.triton.msa.triton_dashboard.project.dto;

import com.triton.msa.triton_dashboard.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectResponseDto(
        Long id,
        String name
) {
    public static ProjectResponseDto from(Project entity) {
        return new ProjectResponseDto(
                entity.getId(),
                entity.getName()
        );
    }
}

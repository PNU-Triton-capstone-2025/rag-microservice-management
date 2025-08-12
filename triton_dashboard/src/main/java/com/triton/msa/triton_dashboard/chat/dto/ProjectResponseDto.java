package com.triton.msa.triton_dashboard.chat.dto;

import com.triton.msa.triton_dashboard.project.entity.Project;

public record ProjectResponseDto(
        Long id,
        String name
) {
    public static ProjectResponseDto from(Project entity) {
        return new ProjectResponseDto(entity.getId(), entity.getName());
    }
}

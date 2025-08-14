package com.triton.msa.triton_dashboard.rag.dto;

import com.triton.msa.triton_dashboard.project.entity.Project;

public record ProjectResponseDto(
        Long id,
        String name
) {
    public static ProjectResponseDto from(Project project) {
        return new ProjectResponseDto(project.fetchId(), project.fetchName());
    }
}

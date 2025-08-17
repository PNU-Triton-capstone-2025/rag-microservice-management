package com.triton.msa.triton_dashboard.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.triton.msa.triton_dashboard.project.entity.Project;

import java.time.format.DateTimeFormatter;

public record ProjectResponseDto(
        Long id,
        String name,
        @JsonProperty("ssh_ip_address")
        String sshIpAddress,
        @JsonProperty("created_at")
        String createdAt

) {
    public static ProjectResponseDto from(Project project) {
        String formattedCreatedAt = project
                .fetchCreatedAt()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new ProjectResponseDto(
                project.fetchId(),
                project.fetchName(),
                project.fetchSshInfo().getSshIpAddress(),
                formattedCreatedAt
        );
    }
}

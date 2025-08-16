package com.triton.msa.triton_dashboard.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.triton.msa.triton_dashboard.ssh.dto.SshInfoCreateRequestDto;

public record ProjectCreateRequestDto(
        String name,
        @JsonProperty("ssh_info")
        SshInfoCreateRequestDto sshInfoCreateRequestDto
) {
    private final static String DEFAULT_NAME = "your_project_name";

    public static ProjectCreateRequestDto getEmpty() {
        return new ProjectCreateRequestDto(
                DEFAULT_NAME,
                SshInfoCreateRequestDto.getEmpty()
        );
    }
}

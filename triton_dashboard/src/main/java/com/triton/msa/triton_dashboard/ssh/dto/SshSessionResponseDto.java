package com.triton.msa.triton_dashboard.ssh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SshSessionResponseDto(
        @JsonProperty("session_id")
        String sessionId
) {
}

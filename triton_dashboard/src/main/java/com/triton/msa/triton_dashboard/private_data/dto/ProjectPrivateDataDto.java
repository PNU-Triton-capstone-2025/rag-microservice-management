package com.triton.msa.triton_dashboard.private_data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ProjectPrivateDataDto(
        Long id,
        @JsonProperty("project_id")
        Long projectId,
        String filename,
        @JsonProperty("content_type")
        String contentType,
        @JsonProperty("created_at")
        Instant createdAt
) {}

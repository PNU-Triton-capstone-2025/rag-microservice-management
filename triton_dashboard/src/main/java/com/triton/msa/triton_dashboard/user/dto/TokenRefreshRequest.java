package com.triton.msa.triton_dashboard.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @JsonProperty("refresh_token")
        @NotBlank
        String refreshToken
) {
}

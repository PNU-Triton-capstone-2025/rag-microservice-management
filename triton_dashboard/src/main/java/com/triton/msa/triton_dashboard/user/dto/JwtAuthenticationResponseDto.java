package com.triton.msa.triton_dashboard.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JwtAuthenticationResponseDto(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken
) {
    public String getTokenType() {
        return "Bearer";
    }
}

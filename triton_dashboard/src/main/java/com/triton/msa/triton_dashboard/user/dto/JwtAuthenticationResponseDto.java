package com.triton.msa.triton_dashboard.user.dto;

public record JwtAuthenticationResponseDto(
        String accessToken,
        String refreshToken
) {
    public String getTokenType() {
        return "Bearer";
    }
}

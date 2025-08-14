package com.triton.msa.triton_dashboard.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}

package com.triton.msa.triton_dashboard.user.dto;

public record ChangePasswordRequestDto(
        String currPassword,
        String newPassword) {}

package com.triton.msa.triton_dashboard.user.dto;

import com.triton.msa.triton_dashboard.user.entity.User;

public record UserResponseDto(
        Long id,
        String username
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.getId(), user.getUsername());
    }
}

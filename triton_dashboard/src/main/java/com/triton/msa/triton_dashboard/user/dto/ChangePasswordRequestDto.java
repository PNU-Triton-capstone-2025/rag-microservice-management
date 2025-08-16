package com.triton.msa.triton_dashboard.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChangePasswordRequestDto(
        @JsonProperty("curr_password")
        String currPassword,
        @JsonProperty("new_password")
        String newPassword
) {

}

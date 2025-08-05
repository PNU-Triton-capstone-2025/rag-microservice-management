package com.triton.msa.triton_dashboard.common.exception;

public record ErrorResponse(
        int status,
        String error,
        String message
) {

}

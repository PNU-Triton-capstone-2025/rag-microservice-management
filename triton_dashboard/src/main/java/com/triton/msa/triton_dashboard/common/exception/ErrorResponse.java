package com.triton.msa.triton_dashboard.common.exception;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        int status,
        String error,
        String message
) {
    public ErrorResponse(HttpStatus status, String message) {
        this(status.value(), status.getReasonPhrase(), message);
    }
}

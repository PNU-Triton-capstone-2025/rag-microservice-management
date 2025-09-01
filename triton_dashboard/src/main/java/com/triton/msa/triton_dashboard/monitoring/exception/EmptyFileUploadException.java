package com.triton.msa.triton_dashboard.monitoring.exception;

public class EmptyFileUploadException extends RuntimeException {
    public EmptyFileUploadException(String message) {
        super(message);
    }
}

package com.triton.msa.triton_dashboard.log_deployer.exception;

public class LogDeploymentException extends RuntimeException {
    public LogDeploymentException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

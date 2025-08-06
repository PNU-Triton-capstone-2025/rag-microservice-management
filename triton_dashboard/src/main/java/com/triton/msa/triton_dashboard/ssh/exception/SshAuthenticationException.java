package com.triton.msa.triton_dashboard.ssh.exception;

public class SshAuthenticationException extends RuntimeException {
    public SshAuthenticationException(String msg) {
        super(msg);
    }

    public SshAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

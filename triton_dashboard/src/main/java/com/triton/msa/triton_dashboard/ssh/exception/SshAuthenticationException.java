package com.triton.msa.triton_dashboard.ssh.exception;

import java.io.IOException;

public class SshAuthenticationException extends IOException {
    public SshAuthenticationException(String msg) {
        super(msg);
    }
}

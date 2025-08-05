package com.triton.msa.triton_dashboard.ssh.exception;

import java.io.IOException;

public class SshConnectionException extends IOException {
    public SshConnectionException(String msg) {
        super(msg);
    }
}

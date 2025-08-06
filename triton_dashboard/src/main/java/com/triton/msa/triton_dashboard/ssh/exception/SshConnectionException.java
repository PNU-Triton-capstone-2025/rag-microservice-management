package com.triton.msa.triton_dashboard.ssh.exception;

public class SshConnectionException extends RuntimeException{
    public SshConnectionException(String msg) {
        super(msg);
    }
}

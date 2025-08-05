package com.triton.msa.triton_dashboard.common.advice;

import com.triton.msa.triton_dashboard.common.exception.ErrorResponse;
import com.triton.msa.triton_dashboard.ssh.exception.SshAuthenticationException;
import com.triton.msa.triton_dashboard.ssh.exception.SshConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SshAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSshAuthException(SshAuthenticationException ex) {
        log.error("SSH Authentication failed: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SshConnectionException.class)
    public ResponseEntity<ErrorResponse> handleSshConnectionException(SshConnectionException ex) {
        log.error("SSH Connection error: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

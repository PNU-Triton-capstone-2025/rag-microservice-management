package com.triton.msa.triton_dashboard.common.advice;

import com.triton.msa.triton_dashboard.ssh.exception.SshAuthenticationException;
import com.triton.msa.triton_dashboard.ssh.exception.SshConnectionException;
import com.triton.msa.triton_dashboard.ssh.exception.SshKeyFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalApiExceptionHandler {

    @ExceptionHandler(SshAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleSshAuthException(SshAuthenticationException ex) {
        log.error("SSH Authentication failed: {}", ex.getMessage());
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SshConnectionException.class)
    public ResponseEntity<Map<String, Object>> handleSshConnectionException(SshConnectionException ex) {
        log.error("SSH Connection error: {}", ex.getMessage());
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
//        log.error("Runtime exception occurred", ex);
//        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler(SshKeyFileException.class)
    public ResponseEntity<Map<String, Object>> handleSshKeyFileException(SshKeyFileException ex) {
        log.error("Failed to generate temporary SSH key file", ex);
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> makeErrorResponseEntity(String msg, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", msg);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}

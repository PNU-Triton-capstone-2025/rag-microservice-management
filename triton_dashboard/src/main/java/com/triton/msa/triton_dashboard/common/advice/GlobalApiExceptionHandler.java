package com.triton.msa.triton_dashboard.common.advice;

import com.triton.msa.triton_dashboard.monitoring.exception.YamlFileException;
import com.triton.msa.triton_dashboard.private_data.exception.PrivateDataUnzipException;
import com.triton.msa.triton_dashboard.private_data.exception.UnsupportedFileTypeException;
import com.triton.msa.triton_dashboard.rag.exception.FileUploadException;
import com.triton.msa.triton_dashboard.ssh.exception.SshAuthenticationException;
import com.triton.msa.triton_dashboard.ssh.exception.SshConnectionException;
import com.triton.msa.triton_dashboard.ssh.exception.SshKeyFileException;
import com.triton.msa.triton_dashboard.user.exception.InvalidApiKeyException;
import com.triton.msa.triton_dashboard.user.exception.InvalidPasswordException;
import com.triton.msa.triton_dashboard.user.exception.UnauthorizedException;
import jakarta.persistence.EntityNotFoundException;
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

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("Unauthorized exception: {}", ex.getMessage());
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPasswordException(InvalidPasswordException ex) {
        log.error("Invalid password exception: {}", ex.getMessage());
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidApiKeyException(InvalidApiKeyException ex) {
        log.error("Invalid api key exception: {}", ex.getMessage());
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PrivateDataUnzipException.class)
    public ResponseEntity<Map<String, Object>> handlePrivateDataUnzipException(PrivateDataUnzipException ex) {
        log.error("Private data unzip exception: {}", ex.getMessage());
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public  ResponseEntity<Map<String, Object>> handleUnsupportedFileTypeException(UnsupportedFileTypeException ex) {
        log.error("Unsupported file type exception: {}", ex.getMessage());
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Map<String, String>> handleFileUploadException(FileUploadException e) {
        Map<String, String> response = Map.of("error", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public  ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found exception: {}", ex.getMessage());
        return makeErrorResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
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

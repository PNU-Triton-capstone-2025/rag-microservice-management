package com.triton.msa.triton_dashboard.user.controller;

import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.dto.UserResponseDto;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.service.UserService;
import com.triton.msa.triton_dashboard.user.util.LlmApiKeyValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;
    private final LlmApiKeyValidator apiKeyValidator;

    @PostMapping("/register")
    public ResponseEntity<?> registerUserAccount(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            DefaultMessageSourceResolvable::getDefaultMessage
                    ));
            return ResponseEntity.badRequest().body(errors);
        }

        User newUser = userService.registerNewUser(registrationDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(newUser));
    }

    @PostMapping("/validate-api-key")
    public ResponseEntity<String> validateApiKey(@RequestBody UserRegistrationDto userRegistrationDto) {
        apiKeyValidator.validateAll(userRegistrationDto);
        return ResponseEntity.ok("API Key is valid.");
    }
}

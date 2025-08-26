package com.triton.msa.triton_dashboard.user.controller;

import com.triton.msa.triton_dashboard.user.dto.ApiKeyValidationRequestDto;
import com.triton.msa.triton_dashboard.user.dto.ChangeApiKeyRequest;
import com.triton.msa.triton_dashboard.user.dto.ChangePasswordRequestDto;
import com.triton.msa.triton_dashboard.user.dto.JwtAuthenticationResponseDto;
import com.triton.msa.triton_dashboard.user.dto.TokenRefreshRequest;
import com.triton.msa.triton_dashboard.user.dto.UserApiKeyRequestDto;
import com.triton.msa.triton_dashboard.user.dto.UserDeleteRequestDto;
import com.triton.msa.triton_dashboard.user.dto.UserLoginRequest;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.dto.UserResponseDto;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.service.TokenService;
import com.triton.msa.triton_dashboard.user.service.UserService;
import com.triton.msa.triton_dashboard.user.util.LlmApiKeyValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserApiController {
    private final UserService userService;
    private final LlmApiKeyValidator apiKeyValidator;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUserAccount(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return manageBindingResultError(bindingResult);
        }

        apiKeyValidator.validateAll(registrationDto);
        User newUser = userService.registerNewUser(registrationDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(newUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginRequest loginRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return manageBindingResultError(bindingResult);
        }
        JwtAuthenticationResponseDto responseDto = tokenService.authenticateAndGetToken(loginRequest.username(), loginRequest.password());

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponseDto> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        JwtAuthenticationResponseDto responseDto = tokenService.reissueToken(request.refreshToken());
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/validate-api-key")
    public ResponseEntity<String> validateApiKey(@RequestBody ApiKeyValidationRequestDto apiKeyValidationRequestDto) {
        apiKeyValidator.validateOne(
                apiKeyValidationRequestDto.provider(),
                apiKeyValidationRequestDto.apiKey()
        );

        return ResponseEntity.ok("API key is valid (or skipped if empty)");
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) UserDeleteRequestDto dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        final String password = (dto != null) ? dto.password() : null;
        userService.deleteCurrentUser(userDetails.getUsername(), password);

        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequestDto dto
    ) {
        userService.updatePassword(userDetails.getUsername(), dto.currPassword(), dto.newPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/api-key")
    public ResponseEntity<Map<LlmProvider, String>> getApiKey(@AuthenticationPrincipal UserDetails userDetails, UserApiKeyRequestDto apiKeyRequestDto) {
        String userApiKey = userService.getCurrentUserApiKey(userDetails.getUsername(), apiKeyRequestDto);
        return ResponseEntity.ok(Map.of(apiKeyRequestDto.provider(), userApiKey));
    }

    @PatchMapping("/me/api-key")
    public ResponseEntity<Void> updateApiKey(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangeApiKeyRequest dto
    ) {
        userService.updateApiKey(userDetails.getUsername(), dto.provider(), dto.newApiKey());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Map<String, String>> manageBindingResultError(BindingResult bindingResult) {
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage
                ));
        return ResponseEntity.badRequest().body(errors);
    }
}

package com.triton.msa.triton_dashboard.user.controller;

import com.triton.msa.triton_dashboard.user.dto.JwtAuthenticationResponseDto;
import com.triton.msa.triton_dashboard.user.dto.UserLoginRequest;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.service.TokenService;
import com.triton.msa.triton_dashboard.user.service.UserService;
import com.triton.msa.triton_dashboard.user.util.LlmApiKeyValidator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LlmApiKeyValidator apiKeyValidator;
    private final TokenService tokenService;

    @GetMapping()
    public String root() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(
            @ModelAttribute UserLoginRequest loginRequest,
            Model model,
            HttpServletResponse response
    ) {
        JwtAuthenticationResponseDto responseDto = tokenService.authenticateAndGetToken(
                loginRequest.username(),
                loginRequest.password()
        );

        Cookie refreshTokenCookie = new Cookie("refreshToken", responseDto.refreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(refreshTokenCookie);

        model.addAttribute("accessToken", responseDto.accessToken());
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", UserRegistrationDto.getEmpty());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistrationDto registrationDto,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        apiKeyValidator.validateAll(registrationDto);

        userService.registerNewUser(registrationDto);
        return "redirect:/";
    }

    @PostMapping("/validate-api-key")
    @ResponseBody
    public ResponseEntity<String> validateApiKey(@RequestBody UserRegistrationDto registrationDto) {
        //apiKeyValidator.validate(registrationDto.aiServiceApiKey(), registrationDto.llmModel());
        return ResponseEntity.ok().body("valid");
    }
}

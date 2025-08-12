package com.triton.msa.triton_dashboard.user.controller;

import com.triton.msa.triton_dashboard.user.dto.ApiKeyValidationResponseDto;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.service.UserService;
import com.triton.msa.triton_dashboard.user.util.LlmApiKeyValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LlmApiKeyValidator apiKeyValidator;

    @GetMapping()
    public String root() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", UserRegistrationDto.getEmpty());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistrationDto dto,
                                      BindingResult bindingResult,
                                      Model model) {
        if (bindingResult.hasErrors()) return "register";

        ApiKeyValidationResponseDto responseDto = apiKeyValidator.validateAll(dto);

        if (!responseDto.allValid()) {
            bindingResult.reject("apiKey.invalid", "입력하신 API 키가 유효하지 않습니다. 아래 상태를 확인하세요.");

            model.addAttribute("validation", responseDto);
            return "register";
        }

        userService.registerNewUser(dto);
        return "redirect:/login/?success";
    }
}

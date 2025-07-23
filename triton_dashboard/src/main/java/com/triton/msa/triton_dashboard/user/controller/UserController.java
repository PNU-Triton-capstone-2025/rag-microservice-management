package com.triton.msa.triton_dashboard.user.controller;

import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.LlmModel;
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

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LlmApiKeyValidator llmApiKeyValidator;

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
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistrationDto registrationDto,
        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.registerNewUser(registrationDto);
        return "redirect:/login/?success";
    }

    @PostMapping("/validate-api-key")
    @ResponseBody
    public ResponseEntity<?> validateApiKey(@RequestBody ApiKeyValidationRequest request) {
        try {
            llmApiKeyValidator.validate(request.apiKey(), request.model());
            return ResponseEntity.ok().body("valid");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    public record ApiKeyValidationRequest(String apiKey, LlmModel model) {}
}

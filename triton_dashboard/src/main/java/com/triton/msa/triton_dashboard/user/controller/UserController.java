package com.triton.msa.triton_dashboard.user.controller;

import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}

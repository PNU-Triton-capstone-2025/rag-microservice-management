package com.triton.msa.triton_dashboard.user.controller;

import com.triton.msa.triton_dashboard.common.config.SecurityConfig;
import com.triton.msa.triton_dashboard.common.jwt.JwtTokenProvider;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.service.TokenService;
import com.triton.msa.triton_dashboard.user.service.UserService;
import com.triton.msa.triton_dashboard.user.util.LlmApiKeyValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController 단위 테스트")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LlmApiKeyValidator llmApiKeyValidator;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @DisplayName("/ - 메인 페이지 뷰")
    void rootView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("/login - 로그인 페이지 뷰")
    void loginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("/register - 회원가입 페이지 뷰 & user 객체")
    void registerForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", UserRegistrationDto.getEmpty()));
    }
}
package com.triton.msa.triton_dashboard.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triton.msa.triton_dashboard.common.config.SecurityConfig;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.ApiKeyInfo;
import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.entity.UserRole;
import com.triton.msa.triton_dashboard.user.service.UserService;
import com.triton.msa.triton_dashboard.user.util.LlmApiKeyValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController 단위 테스트")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LlmApiKeyValidator apiKeyValidator;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @DisplayName("/register - 유효한 post 요청 시 리다이렉트")
    void registerAndRedirect() throws Exception {
        // given
        UserRegistrationDto registrationDto = new UserRegistrationDto("testUser", "password123", "api-key", LlmModel.GPT_4O);
        when(userService.registerNewUser(any(UserRegistrationDto.class))).thenReturn(new User("test", "password", new ApiKeyInfo(), Collections.singleton(UserRole.USER)));

        // when & then
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", registrationDto.username())
                .param("password", registrationDto.password())
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/?success"));

        verify(userService, times(1)).registerNewUser(any(UserRegistrationDto.class));
    }

    @Test
    @DisplayName("API 키 유효성 검증 성공 - 200")
    void validateApiKey() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto("user", "pass", "valid-key", LlmModel.GPT_4O);
        doNothing().when(apiKeyValidator).validate(dto.aiServiceApiKey(), dto.llmModel());

        mockMvc.perform(post("/validate-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("valid"));

        verify(apiKeyValidator, times(1)).validate(dto.aiServiceApiKey(), dto.llmModel());
    }

    @Test
    @DisplayName("API 키 유효성 검증 실패 - 401")
    void validateApiKeyFailed() throws Exception {
        // given
        UserRegistrationDto dto = new UserRegistrationDto("user", "pass", "invalid-key", LlmModel.GPT_4O);
        String errMsg = "API 키가 유효하지 않습니다.";

        doThrow(new IllegalArgumentException(errMsg)).when(apiKeyValidator).validate(dto.aiServiceApiKey(), dto.llmModel());

        // when & then
        mockMvc.perform(post("/validate-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(errMsg));

        verify(apiKeyValidator, times(1)).validate(dto.aiServiceApiKey(), dto.llmModel());
    }
}
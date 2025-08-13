package com.triton.msa.triton_dashboard.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triton.msa.triton_dashboard.common.config.SecurityConfig;
import com.triton.msa.triton_dashboard.user.dto.ApiKeyValidationResponseDto;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.ApiKeyInfo;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.entity.UserRole;
import com.triton.msa.triton_dashboard.user.exception.ApiKeysValidationException;
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
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
        doNothing().when(apiKeyValidator).validateAll(any(UserRegistrationDto.class));
        when(userService.registerNewUser(any(UserRegistrationDto.class)))
                .thenReturn(new User("test","password",
                        Set.of(new ApiKeyInfo("api-key", LlmProvider.OPENAI)),
                        Collections.singleton(UserRole.USER)));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("password", "password123")
                        .param("apiKeys[OPENAI]", "api-key")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/?success"));

        verify(apiKeyValidator, times(1)).validateAll(any(UserRegistrationDto.class));
        verify(userService, times(1)).registerNewUser(any(UserRegistrationDto.class));
    }

    @Test
    @DisplayName("/register - API 키 검증 실패 시 register 뷰로 되돌림")
    void registerWithInvalidApiKeysReturnRegisterView() throws Exception {
        // given
        Map<String, Object> results = new LinkedHashMap<>();
        results.put(LlmProvider.OPENAI.name(), "error: API 키가 유효하지 않습니다");
        ApiKeyValidationResponseDto dto = new ApiKeyValidationResponseDto(results);
        ApiKeysValidationException ex = new ApiKeysValidationException(dto,
                new UserRegistrationDto("badUser", "pw",
                new EnumMap<LlmProvider, String>(LlmProvider.class)));

        doThrow(ex).when(apiKeyValidator).validateAll(any(UserRegistrationDto.class));

        // when & then
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "badUser")
                .param("password", "pw")
                .param("apiKeys[OPENAI]", "wrong-key")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("register"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attributeExists("validation"))
            .andExpect(model().attributeExists("errorMessage"));

        // 유저 생성이 호출되면 안됨
        verify(userService, never()).registerNewUser(any(UserRegistrationDto.class));
    }
}
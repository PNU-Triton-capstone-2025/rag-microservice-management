package com.triton.msa.triton_dashboard.user;

import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.repository.UserRepository;
import com.triton.msa.triton_dashboard.user.util.LlmApiKeyValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User 통합 테스트")
public class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private LlmApiKeyValidator llmApiKeyValidator;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    /*
    @Test
    @DisplayName("회원가입")
    void registerUser() throws Exception {
        // given
        String username = "test_user";
        String password = "password123";

        // when
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("password", password)
                .param("aiServiceApiKey", "some-key")
                .param("llmModel", "GPT_4O")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/?success"));

        // then
        Optional<User> foundUser = userRepository.findByUsername(username);
        assertThat(foundUser).isPresent();

        User user = foundUser.get();

        assertThat(user.getUsername()).isEqualTo(username);
        assertTrue(passwordEncoder.matches(password, user.getPassword()));
        //assertThat(user.getApiKeyInfo().getLlmModel()).isEqualTo(LlmModel.GPT_4O);
    }
     */

}

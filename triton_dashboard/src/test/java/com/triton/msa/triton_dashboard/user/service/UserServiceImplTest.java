package com.triton.msa.triton_dashboard.user.service;

import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.ApiKeyInfo;
import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.entity.UserRole;
import com.triton.msa.triton_dashboard.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 단위 테스트")
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Test
    @DisplayName("User 등록")
    void registerUser() {
        // given
        Map<LlmProvider, String> map = new EnumMap<>(LlmProvider.class);
        UserRegistrationDto registrationDto = new UserRegistrationDto("newUser", "password", map);
        String encodedPassword = "samplePassword";
        when(passwordEncoder.encode(registrationDto.password())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(new User(
                "newUser",
                "password",
                Set.of(new ApiKeyInfo("", LlmProvider.GEMINI)),
                Collections.singleton(UserRole.USER)
        ));

        // when
        User user = userService.registerNewUser(registrationDto);

        // then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User captured = userArgumentCaptor.getValue();

        assertThat(captured.getUsername())
                .isEqualTo(registrationDto.username());
        assertThat(captured.getPassword())
                .isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("사용자 이름으로 User 조회")
    void getUser() {
        // given
        User user = new User(
                "newUser",
                "encodedPassword",
                Collections.emptySet(),
                Collections.singleton(UserRole.USER)
        );

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.of(user));

        // when
        User found = userService.getUser("newUser");

        // then
        assertThat(found.getUsername()).isEqualTo("newUser");

    }

    @Test
    @DisplayName("사용자 이름으로 UserDetails 조회")
    void getUserDetails() {
        // given
        User user = new User(
                "newUser",
                "encodedPassword",
                Collections.emptySet(),
                Collections.singleton(UserRole.USER)
        );

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.of(user));

        // when
        UserDetails userDetails = userService.loadUserByUsername("newUser");

        // then
        assertThat(userDetails.getUsername())
                .isEqualTo("newUser");
        assertThat(userDetails.getPassword())
                .isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 이름 조회 - UsernameNotFoundException")
    void loadUserByUsernameFailed() {
        // given
        when(userRepository.findByUsername("notValid")).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("notValid");
        });
    }
}
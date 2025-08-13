package com.triton.msa.triton_dashboard.user.service;

import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.ApiKeyInfo;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.entity.UserRole;
import com.triton.msa.triton_dashboard.user.exception.InvalidApiKeyException;
import com.triton.msa.triton_dashboard.user.exception.InvalidPasswordException;
import com.triton.msa.triton_dashboard.user.exception.UnauthorizedException;
import com.triton.msa.triton_dashboard.user.repository.UserRepository;
import com.triton.msa.triton_dashboard.user.util.LlmApiKeyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LlmApiKeyValidator llmApiKeyValidator;

    @Override
    @Transactional
    public User registerNewUser(UserRegistrationDto dto) {
        Set<ApiKeyInfo> keys = new HashSet<>();
        for (LlmProvider p : LlmProvider.values()) {
            if (dto.apiKeyOf(p) != null && !dto.apiKeyOf(p).isBlank()) {
                keys.add(new ApiKeyInfo(dto.apiKeys().get(p), p));
            }
        }

        User user = new User(
                dto.username(),
                passwordEncoder.encode(dto.password()),
                keys,
                Collections.singleton(UserRole.USER)
        );
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("Role_" + role.name()))
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public User getUser(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public void deleteCurrentUser(String rawPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails details)) {
            throw new UnauthorizedException("로그인 정보가 없습니다.");
        }

        User user = userRepository.findByUsername(details.getUsername())
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다." + details.getUsername()));

        if (rawPassword != null && !rawPassword.isBlank()) {
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
            }
        }

        userRepository.deleteById(user.getId());
    }

    @Override
    @Transactional
    public void updatePassword(String currPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidPasswordException("새 비밀번호가 비어있습니다.");
        }

        User me = getCurrentUserOrThrow();

        if (!passwordEncoder.matches(currPassword, me.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }

        me.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Override
    @Transactional
    public void updateApiKey(LlmProvider provider, String newApiKey) {
        if (provider == null) {
            throw new InvalidApiKeyException("provider 입력은 필수입니다.");
        }
        if (newApiKey == null || newApiKey.isBlank()) {
            throw new InvalidApiKeyException("새 API 키 입력은 필수입니다.");
        }

        llmApiKeyValidator.validateOne(provider, newApiKey);

        User me = getCurrentUserOrThrow();

        me.getApiKeys().removeIf(k -> k.getProvider() == provider);
        me.getApiKeys().add(new ApiKeyInfo(newApiKey, provider));
    }

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails ud)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return userRepository.findByUsername(ud.getUsername())
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
    }
}

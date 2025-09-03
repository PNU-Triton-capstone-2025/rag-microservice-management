package com.triton.msa.triton_dashboard.user.service;

import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import com.triton.msa.triton_dashboard.user.dto.UserApiKeyRequestDto;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.ApiKeyInfo;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.entity.UserRole;
import com.triton.msa.triton_dashboard.user.exception.InvalidApiKeyException;
import com.triton.msa.triton_dashboard.user.exception.InvalidPasswordException;
import com.triton.msa.triton_dashboard.user.exception.UnauthorizedException;
import com.triton.msa.triton_dashboard.user.repository.UserRepository;
import com.triton.msa.triton_dashboard.user.util.ApiKeyEncryptor;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;
    private final LlmApiKeyValidator llmApiKeyValidator;
    private final ApiKeyEncryptor apiKeyEncryptor;

    @Override
    @Transactional
    public User registerNewUser(UserRegistrationDto dto) {
        Set<ApiKeyInfo> keys = new HashSet<>();
        for (LlmProvider p : LlmProvider.values()) {
            if (dto.apiKeyOf(p) != null && !dto.apiKeyOf(p).isBlank()) {
                String encryptedApiKey = apiKeyEncryptor.encrypt(dto.apiKeyOf(p));
                keys.add(new ApiKeyInfo(encryptedApiKey, p));
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
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
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
    public void deleteCurrentUser(String username, String rawPassword) {
        User user = getUser(username);

        if (rawPassword != null && !rawPassword.isBlank()) {
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
            }
        }
        userRepository.deleteById(user.getId());
    }

    @Override
    @Transactional
    public void updatePassword(String username, String currPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidPasswordException("새 비밀번호가 비어있습니다.");
        }
        User me = getUser(username);

        if (!passwordEncoder.matches(currPassword, me.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }
        me.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Override
    @Transactional
    public void updateApiKey(String username, LlmProvider provider, String newApiKey) {
        if (provider == null) {
            throw new InvalidApiKeyException("provider 입력은 필수입니다.");
        }
        if (newApiKey == null || newApiKey.isBlank()) {
            throw new InvalidApiKeyException("새 API 키 입력은 필수입니다.");
        }
        llmApiKeyValidator.validateOne(provider, newApiKey);

        User me = getUser(username);

        me.getApiKeys().removeIf(k -> k.getProvider() == provider);
        me.getApiKeys().add(new ApiKeyInfo(apiKeyEncryptor.encrypt(newApiKey), provider));
    }

    @Override
    @Transactional(readOnly = true)
    public String getCurrentUserApiKey(String username, UserApiKeyRequestDto apiKeyRequestDto) {
        User me = getUser(username);

        return me.getApiKeys().stream()
                .filter(apiKeyInfo -> apiKeyInfo.getProvider() == apiKeyRequestDto.provider())
                .map(apiKeyInfo -> apiKeyEncryptor.decrypt(apiKeyInfo.getApiKey()))
                .findFirst()
                .orElseThrow(() -> new InvalidApiKeyException(apiKeyRequestDto.provider().toValue() + " 에 해당하는 API KEY가 없습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByProjectId(Long projectId) {
        Project project = projectRepository.findByIdWithUser(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        return project.fetchUser();
    }
}

package com.triton.msa.triton_dashboard.user.service;

import com.triton.msa.triton_dashboard.user.dto.UserApiKeyRequestDto;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

public interface UserService extends UserDetailsService {
    User registerNewUser(UserRegistrationDto registrationDto);
    User getUser(String username);
    void deleteCurrentUser(String username, String rawPassword);
    void updatePassword(String username, String rawPassword, String newPassword);
    void updateApiKey(String username, LlmProvider provider, String newApiKey);
    String getCurrentUserApiKey(String username, UserApiKeyRequestDto apiKeyRequestDto);
    User getUserByProjectId(Long projectId);
}

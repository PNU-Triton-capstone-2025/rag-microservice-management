package com.triton.msa.triton_dashboard.user.service;

import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.ApiKeyInfo;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.entity.UserRole;
import com.triton.msa.triton_dashboard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override @Transactional
    public User registerNewUser(UserRegistrationDto dto) {
        Set<ApiKeyInfo> keys = new HashSet<>();
        if (dto.openaiApiKey()!=null && !dto.openaiApiKey().isBlank())
            keys.add(new ApiKeyInfo(dto.openaiApiKey(), LlmProvider.OPENAI));
        if (dto.anthropicApiKey()!=null && !dto.anthropicApiKey().isBlank())
            keys.add(new ApiKeyInfo(dto.anthropicApiKey(), LlmProvider.ANTHROPIC));
        if (dto.googleApiKey()!=null && !dto.googleApiKey().isBlank())
            keys.add(new ApiKeyInfo(dto.googleApiKey(), LlmProvider.GOOGLE));
        if (dto.grokApiKey()!=null && !dto.grokApiKey().isBlank())
            keys.add(new ApiKeyInfo(dto.grokApiKey(), LlmProvider.GROK));

        User user = new User(
                dto.username(),
                passwordEncoder.encode(dto.password()),
                keys,
                Collections.singleton(UserRole.USER)
        );
        return userRepository.save(user);
    }

    @Override
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

    public User getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}

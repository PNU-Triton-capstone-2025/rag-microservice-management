package com.triton.msa.triton_dashboard.user.service;

import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User registerNewUser(UserRegistrationDto registrationDto);
    public String authenticateAndGetToken(String username, String password);
    public User getUser(String username);
}

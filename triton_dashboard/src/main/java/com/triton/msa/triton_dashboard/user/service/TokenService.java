package com.triton.msa.triton_dashboard.user.service;

import com.triton.msa.triton_dashboard.common.jwt.JwtTokenProvider;
import com.triton.msa.triton_dashboard.user.dto.JwtAuthenticationResponseDto;
import com.triton.msa.triton_dashboard.user.entity.RefreshToken;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.repository.RefreshTokenRepository;
import com.triton.msa.triton_dashboard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Transactional
    public JwtAuthenticationResponseDto authenticateAndGetToken(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.createToken(authentication);
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(authentication);

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        refreshToken -> refreshToken.updateToken(refreshTokenValue),
                        () -> refreshTokenRepository.save(new RefreshToken(user, refreshTokenValue))
                );

        return new JwtAuthenticationResponseDto(accessToken, refreshTokenValue);
    }

    public JwtAuthenticationResponseDto reissueToken(String requestRefreshToken) {
        if(!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token not found in DB"));

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken.retrieveToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<GrantedAuthority> authoritySet = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(user.getUsername(), "", authoritySet),
                null,
                authoritySet
        );

        String newAccessToken = jwtTokenProvider.createToken(authentication);

        return new JwtAuthenticationResponseDto(newAccessToken, requestRefreshToken);
    }
}

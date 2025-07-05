package com.triton.msa.triton_dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css**", "/js/**", "/images/**", "/webjars/**")
                .permitAll()
                .requestMatchers("/", "/register", "/login")
                .permitAll()
                .requestMatchers("/h2-console/**")
                .permitAll()
                .anyRequest().authenticated()
        )
        .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/projects", true)
                .permitAll()
        )
        .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .permitAll()
        )
        .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**"))
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}

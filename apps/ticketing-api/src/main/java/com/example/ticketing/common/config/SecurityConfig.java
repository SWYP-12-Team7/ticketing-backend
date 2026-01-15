package com.example.ticketing.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final String[] PUBLIC_ENDPOINTS = {
      "/actuator/health",
      "/error",
      "/",
      "/auth/**"  // 인증 관련 엔드포인트는 모두 허용
  };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
            .anyRequest().authenticated()
        )
        .build();
  }
}

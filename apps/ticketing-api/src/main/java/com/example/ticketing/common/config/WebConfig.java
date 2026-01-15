package com.example.ticketing.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 설정
 * - CORS 설정: 프론트엔드에서 API 호출 허용
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${cors.allowed-origins:http://localhost:3000}")
  private String[] allowedOrigins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(allowedOrigins)  // 허용할 프론트엔드 도메인
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Authorization")
        .allowCredentials(true)
        .maxAge(3600);
  }
}

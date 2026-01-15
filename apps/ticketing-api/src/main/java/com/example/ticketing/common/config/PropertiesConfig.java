package com.example.ticketing.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties 클래스 활성화 설정
 */
@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    KakaoOAuthProperties.class
})
public class PropertiesConfig {
}

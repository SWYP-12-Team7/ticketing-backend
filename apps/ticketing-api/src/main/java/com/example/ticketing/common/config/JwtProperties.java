package com.example.ticketing.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * JWT 설정 Properties
 * application.yml의 jwt 설정을 바인딩
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Long accessTokenValidity,
        Long refreshTokenValidity
) {
}

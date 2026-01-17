package com.example.ticketing.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * 카카오 OAuth 설정 Properties
 * application.yml의 kakao.oauth 설정을 바인딩
 */
@ConfigurationProperties(prefix = "kakao.oauth")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String tokenUri,
        String userInfoUri
) {
}

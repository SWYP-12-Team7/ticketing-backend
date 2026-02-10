package com.example.ticketing.user.infrastructure.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 액세스 토큰으로 사용자 정보를 요청했을 때 받는 응답
 */
public record KakaoUserInfoResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {

    public record KakaoAccount(
            @JsonProperty("profile")
            Profile profile,

            @JsonProperty("email")
            String email,

            @JsonProperty("name")
            String name
    ) {
    }

    public record Profile(
            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {
    }

    /**
     * 이메일 추출 (전체 이메일 또는 @ 앞부분만)
     * @param useFullEmail true면 전체 이메일, false면 @ 앞부분만
     * @return 이메일
     */
    public String extractEmail(boolean useFullEmail) {
//        String email = kakaoAccount.email();
//        if (useFullEmail) {
//            return email;
//        }
//        // @ 앞부분만 추출
//        return email.substring(0, email.indexOf("@"));

        if (kakaoAccount == null || kakaoAccount.email() == null) {
            return null;
        }

        String email = kakaoAccount.email();

        if (useFullEmail) {
            return email;
        }

        int atIndex = email.indexOf("@");
        if (atIndex == -1) {
            return email; // @ 없으면 전체 반환
        }

        return email.substring(0, atIndex);
    }

    /**
     * 닉네임 추출
     */
    public String getNickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }

    /**
     * 프로필 이미지 URL 추출
     */
    public String getProfileImageUrl() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().profileImageUrl();
    }

    public String getName() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.name();
    }
}

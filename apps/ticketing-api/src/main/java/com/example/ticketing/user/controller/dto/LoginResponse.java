package com.example.ticketing.user.controller.dto;

import com.example.ticketing.user.application.dto.LoginResult;


public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {

  public record UserInfo(
          Long id,
          String email,
          String nickname,
          String profileImage,
          boolean onboardingCompleted
  ) {
  }

  public static LoginResponse from(LoginResult result) {
    return new LoginResponse(
            result.accessToken(),
            result.refreshToken(),
            new UserInfo(
                    result.user().id(),
                    result.user().email(),
                    result.user().nickname(),
                    result.user().profileImage(),
                    result.user().onboardingCompleted()
            )
    );
  }
}

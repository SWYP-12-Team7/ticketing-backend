package com.example.ticketing.common.security;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @CurrentUser 애노테이션이 있는지 확인
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
               parameter.getParameterType().equals(User.class);
    }


    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        // SecurityContext에서 userId 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean required = parameter.getParameterAnnotation(CurrentUser.class).required();

        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            if (required) {
                throw new CustomException(ErrorCode.USER_NOT_FOUND, "인증이 필요합니다.");
            }
            return null;
        }

        Long userId = (Long) authentication.getPrincipal();

        // User 조회
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}

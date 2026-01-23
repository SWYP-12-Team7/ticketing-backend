package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserCategory;
import com.example.ticketing.user.domain.UserCategoryRepository;
import com.example.ticketing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveOnboardingStep1UseCase {

    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;

    public void execute(Long userId, List<String> categories) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (categories.size() < 3 || categories.size() > 10) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "카테고리는 최소 3개, 최대 10개 선택해야 합니다.");
        }

        userCategoryRepository.deleteByUserId(userId);

        List<UserCategory> userCategories = categories.stream()
                .map(category -> UserCategory.builder()
                                .userId(userId)
                                .category(category)
                                .build())
                .toList();

        userCategoryRepository.saveAll(userCategories);

        // 온보딩 진행 상태 업데이트 (step2로 이동)
        user.updateOnboardingStep(2);
    }
}

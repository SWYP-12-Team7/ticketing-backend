package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 회원탈퇴 UseCase
 * Hard Delete 방식으로 처리 (DB에서 완전 삭제)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawUserUseCase {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final UserRecentViewRepository userRecentViewRepository;
    private final UserPreferredRegionRepository userPreferredRegionRepository;
    private final UserCategoryPreferenceRepository userCategoryPreferenceRepository;
    private final FavoriteFolderRepository favoriteFolderRepository;

    public void execute(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 연관 데이터 삭제
        socialAccountRepository.deleteByUserId(userId);
        userFavoriteRepository.deleteByUserId(userId);
        userRecentViewRepository.deleteByUserId(userId);
        userPreferredRegionRepository.deleteByUserId(userId);
        userCategoryPreferenceRepository.deleteByUserId(userId);
        favoriteFolderRepository.deleteByUserId(userId);

        // 사용자 삭제
        userRepository.delete(user);
    }
}

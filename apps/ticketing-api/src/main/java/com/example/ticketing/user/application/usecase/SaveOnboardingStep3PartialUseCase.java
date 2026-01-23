package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Step3 취향 조사 중간 저장 UseCase
 * - 매 응답(좋아요/싫어요)마다 호출되어 자동 저장
 * - 중간 이탈 시에도 데이터 보존
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SaveOnboardingStep3PartialUseCase {

    private final UserRepository userRepository;
    private final UserContentPreferenceRepository preferenceRepository;

    public void execute(Long userId, PreferenceDto preference, int contentIndex) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 해당 콘텐츠에 대한 기존 응답이 있으면 삭제 (재응답 대응)
        preferenceRepository.deleteByUserIdAndContentId(userId, preference.contentId());

        // 새 응답 저장
        UserContentPreference userPreference = UserContentPreference.builder()
                .userId(userId)
                .contentId(preference.contentId())
                .contentType(preference.contentType())
                .preference(preference.preference())
                .build();

        preferenceRepository.save(userPreference);

        // 마지막 응답 인덱스 업데이트
        user.updateLastContentIndex(contentIndex);
    }

    public record PreferenceDto(
            Long contentId,
            ContentType contentType,
            PreferenceType preference
    ) {}
}
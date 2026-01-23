package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.application.dto.RegionDto;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRegion;
import com.example.ticketing.user.domain.UserRegionRepository;
import com.example.ticketing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveOnboardingStep2UseCase {

    private final UserRepository userRepository;
    private final UserRegionRepository userRegionRepository;

    public void execute(Long userId, List<RegionDto> regions, Integer maxTravelTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 지역 개수 검증 (최소 1개, 최대 3개)
        if (regions == null || regions.isEmpty() || regions.size() > 3) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "지역은 최소 1개, 최대 3개 선택해야 합니다.");
        }

        // 기존 지역 삭제
        userRegionRepository.deleteById(userId);


        List<UserRegion> userRegions = regions.stream()
                .map(region -> UserRegion.builder()
                        .userId(userId)
                        .address(region.address())
                        .latitude(region.latitude())
                        .longitude(region.longitude())
                        .tag(region.tag())
                        .build())
                .toList();

        userRegionRepository.saveAll(userRegions);

        user.updateMaxTravelTime(maxTravelTime);

        // 온보딩 진행 상태 업데이트 (step3로 이동)
        user.updateOnboardingStep(3);
        user.updateLastContentIndex(0);  // step3 시작점 초기화
    }
}

package com.example.ticketing.curation.service;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.CurationStatus;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.Exhibition;
import com.example.ticketing.curation.dto.ExhibitionDetailResponse;
import com.example.ticketing.curation.dto.ExhibitionListResponse;
import com.example.ticketing.curation.dto.ExhibitionSummary;
import com.example.ticketing.curation.dto.Pagination;
import com.example.ticketing.curation.event.CurationViewedEvent;
import com.example.ticketing.curation.repository.CurationLikeRepository;
import com.example.ticketing.curation.repository.ExhibitionRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ExhibitionListResponse getExhibitions(
        String keyword,
        String region,
        CurationStatus status,
        Long userId,
        Pageable pageable
    ) {
        Page<Exhibition> exhibitionPage = exhibitionRepository.findAllWithFilters(
            keyword, region, status, pageable
        );

        List<Long> exhibitionIds = exhibitionPage.getContent().stream()
            .map(Exhibition::getId)
            .toList();

        Set<Long> likedExhibitionIds = getLikedExhibitionIds(userId, exhibitionIds);

        List<ExhibitionSummary> summaries = exhibitionPage.getContent().stream()
            .map(exhibition -> ExhibitionSummary.from(
                exhibition,
                likedExhibitionIds.contains(exhibition.getId())
            ))
            .toList();

        return ExhibitionListResponse.of(summaries, Pagination.from(exhibitionPage));
    }


    @Transactional
    public ExhibitionDetailResponse getExhibition(Long exhibitionId, Long userId) {
        Exhibition exhibition = exhibitionRepository.findByIdAndNotDeleted(exhibitionId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXHIBITION_NOT_FOUND));

        exhibition.incrementViewCount();

        // 비동기로 조회 이력 기록 (메인 스레드 블로킹 방지)
        eventPublisher.publishEvent(new CurationViewedEvent(
                exhibitionId,
                CurationType.EXHIBITION,
                userId
        ));

        boolean isLiked = userId != null && userFavoriteRepository.existsByUserIdAndCurationIdAndCurationType(
                userId, exhibitionId, CurationType.EXHIBITION);

        return ExhibitionDetailResponse.from(exhibition, isLiked);
    }

    //전시 목록 조회 시 각 전시의 "좋아요 여부"를 표시
    private Set<Long> getLikedExhibitionIds(Long userId, List<Long> exhibitionIds) {
        if (userId == null || exhibitionIds.isEmpty()) {
            return Collections.emptySet();
        }
        return Set.copyOf(
            userFavoriteRepository.findCurationIdsByUserIdAndCurationIdInAndCurationType(
                userId, exhibitionIds, CurationType.EXHIBITION)
        );
    }
}

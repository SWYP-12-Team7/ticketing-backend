package com.example.ticketing.user.application.usecase;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.application.dto.TimelineResponse;
import com.example.ticketing.user.application.dto.TimelineResponse.TimelineItem;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTimelineUseCase {

    private final UserFavoriteRepository userFavoriteRepository;
    private final CurationRepository curationRepository;

    public TimelineResponse execute(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 사용자의 모든 찜 목록 조회
        List<UserFavorite> favorites = userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, null);

        if (favorites.isEmpty()) {
            return new TimelineResponse(List.of(), List.of());
        }

        List<Long> curationIds = favorites.stream()
                .map(UserFavorite::getCurationId)
                .toList();

        // 2. Curation 정보 조회
        List<Curation> curations = curationRepository.findAllById(curationIds);

        // 3. 오픈 예정 / 진행중 분류
        List<TimelineItem> upcoming = curations.stream()
                .filter(c -> c.getStartDate() != null && c.getStartDate().isAfter(today))
                .sorted((a, b) -> a.getStartDate().compareTo(b.getStartDate())) // 시작일 빠른 순
                .map(TimelineItem::from)
                .toList();

        List<TimelineItem> ongoing = curations.stream()
                .filter(c -> isOngoing(c, today))
                .sorted((a, b) -> {
                    // 종료일 빠른 순 (곧 끝나는 것 먼저)
                    if (a.getEndDate() == null) return 1;
                    if (b.getEndDate() == null) return -1;
                    return a.getEndDate().compareTo(b.getEndDate());
                })
                .map(TimelineItem::from)
                .toList();

        return new TimelineResponse(upcoming, ongoing);
    }

    private boolean isOngoing(Curation curation, LocalDate today) {
        LocalDate start = curation.getStartDate();
        LocalDate end = curation.getEndDate();

        // 시작일이 오늘 이전이거나 같고, 종료일이 오늘 이후이거나 같으면 진행중
        boolean startedOrToday = (start == null || !start.isAfter(today));
        boolean notEnded = (end == null || !end.isBefore(today));

        return startedOrToday && notEnded;
    }
}